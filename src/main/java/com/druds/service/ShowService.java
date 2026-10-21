package com.druds.service;

import com.druds.dto.ShowDTO;
import com.druds.exception.DataBloqueadaException;
import com.druds.exception.ShowNotFoundException;
import com.druds.model.Dj;
import com.druds.model.Show;
import com.druds.model.StatusShow;
import com.druds.repository.BloqueioAgendaRepository;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Cadastro, edição, listagem e remoção de shows, com espelho no Google Agenda. */
@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final GoogleCalendarService googleCalendarService;

    public ShowService(ShowRepository showRepository,
                       BloqueioAgendaRepository bloqueioRepository,
                       GoogleCalendarService googleCalendarService) {
        this.showRepository = showRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.googleCalendarService = googleCalendarService;
    }

    public ShowDTO criar(ShowDTO dto) {
        Show show = new Show();
        ShowMapper.copiarParaEntidade(dto, show);
        if (show.getStatus() == null || show.getStatus().isBlank()) {
            show.setStatus(StatusShow.inicial(show.getData(), LocalDate.now()));
        }
        if (bloqueioRepository.existeConflito(show.getData(), Dj.normalizar(show.getDj()))) {
            throw new DataBloqueadaException(show.getData());
        }
        return paraDTO(salvarESincronizar(show));
    }

    public ShowDTO atualizar(Long id, ShowDTO dto) {
        Show show = buscar(id);
        ShowMapper.copiarParaEntidade(dto, show);
        return paraDTO(salvarESincronizar(show));
    }

    public List<ShowDTO> listarTodos(String dj) {
        String djFiltro = Dj.normalizar(dj);
        return ShowMapper.paraDTOs(showRepository.findByDjOrderByDataAsc(djFiltro), djFiltro);
    }

    public void deletar(Long id) {
        Show show = buscar(id);
        if (show.getGoogleEventId() != null) {
            googleCalendarService.apagarEvento(show.getDj(), show.getGoogleEventId());
        }
        showRepository.delete(show);
    }

    public int deletarDuplicados(String dj) {
        List<Show> duplicados = showRepository.findDuplicados(Dj.normalizar(dj));
        showRepository.deleteAll(duplicados);
        return duplicados.size();
    }

    /**
     * Acerta status e mês/ano de todos os shows conforme as datas passam
     * (ver {@link StatusShow#corrigido}). Roda todo dia e ao subir a aplicação.
     *
     * @return quantos shows mudaram
     */
    public int corrigirStatusEMes() {
        LocalDate hoje = LocalDate.now();
        List<Show> alterados = showRepository.findAll().stream()
                .filter(s -> s.getData() != null)
                .filter(s -> corrigir(s, hoje))
                .toList();
        showRepository.saveAll(alterados);
        return alterados.size();
    }

    private boolean corrigir(Show show, LocalDate hoje) {
        String status = StatusShow.corrigido(show.getStatus(), show.getData(), hoje);
        int mes = show.getData().getMonthValue();
        int ano = show.getData().getYear();
        boolean mudou = !Objects.equals(status, show.getStatus())
                || !Objects.equals(mes, show.getMes())
                || !Objects.equals(ano, show.getAno());
        show.setStatus(status);
        show.setMes(mes);
        show.setAno(ano);
        return mudou;
    }

    /** Show isolado: o transporte do dia aparece nele, mesmo que haja outro show na mesma data. */
    private ShowDTO paraDTO(Show show) {
        return ShowMapper.paraDTOs(List.of(show), show.getDj()).get(0);
    }

    private Show buscar(Long id) {
        return showRepository.findById(id).orElseThrow(() -> new ShowNotFoundException(id));
    }

    /** Salva o show e cria ou atualiza o evento correspondente no Google Agenda. */
    private Show salvarESincronizar(Show show) {
        Show salvo = showRepository.save(show);
        if (salvo.getGoogleEventId() != null) {
            googleCalendarService.atualizarEvento(salvo);
            return salvo;
        }
        String eventId = googleCalendarService.criarEvento(salvo);
        if (eventId == null) return salvo;
        salvo.setGoogleEventId(eventId);
        return showRepository.save(salvo);
    }
}
