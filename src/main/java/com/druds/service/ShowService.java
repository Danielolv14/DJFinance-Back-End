package com.druds.service;

import com.druds.dto.ShowDTO;
import com.druds.exception.ShowNotFoundException;
import com.druds.model.Show;
import com.druds.repository.BloqueioAgendaRepository;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowService {

    private final ShowRepository showRepository;
    private final BloqueioAgendaRepository bloqueioRepository;

    public ShowService(ShowRepository showRepository, BloqueioAgendaRepository bloqueioRepository) {
        this.showRepository = showRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    public ShowDTO criar(ShowDTO dto) {
        Show show = toEntity(dto);

        // Auto-status: PENDENTE se futuro, CONFIRMADO se já passou
        if (show.getStatus() == null || show.getStatus().isBlank()) {
            LocalDate hoje = LocalDate.now();
            show.setStatus(show.getData().isAfter(hoje) ? "PENDENTE" : "CONFIRMADO");
        }

        // Validar bloqueio de agenda
        if (bloqueioRepository.existeConflito(show.getData())) {
            throw new IllegalStateException("A data " + show.getData() + " está bloqueada na agenda.");
        }

        return toDTO(showRepository.save(show));
    }

    public ShowDTO atualizar(Long id, ShowDTO dto) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        atualizarEntidade(show, dto);
        return toDTO(showRepository.save(show));
    }

    public List<ShowDTO> listarTodos() {
        LocalDate hoje = LocalDate.now();
        List<Show> shows = showRepository.findAll();

        // Auto-sincronizar status e mes/ano com a data real
        List<Show> paraAtualizar = shows.stream()
                .filter(s -> {
                    boolean futuroConfirmado = "CONFIRMADO".equals(s.getStatus()) && s.getData().isAfter(hoje);
                    boolean passadoPendente  = "PENDENTE".equals(s.getStatus())   && !s.getData().isAfter(hoje);
                    boolean mesErrado = s.getMes() == null || s.getAno() == null
                                    || s.getData().getMonthValue() != s.getMes()
                                    || s.getData().getYear() != s.getAno();
                    return futuroConfirmado || passadoPendente || mesErrado;
                })
                .collect(Collectors.toList());

        if (!paraAtualizar.isEmpty()) {
            paraAtualizar.forEach(s -> {
                if (s.getData().isAfter(hoje) && !"CANCELADO".equals(s.getStatus())) {
                    s.setStatus("PENDENTE");
                } else if (!s.getData().isAfter(hoje) && "PENDENTE".equals(s.getStatus())) {
                    s.setStatus("CONFIRMADO");
                }
                s.setMes(s.getData().getMonthValue());
                s.setAno(s.getData().getYear());
            });
            showRepository.saveAll(paraAtualizar);
        }

        return shows.stream().map(this::toDTO).toList();
    }

    public ShowDTO buscarPorId(Long id) {
        return toDTO(showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id)));
    }

    public void deletar(Long id) {
        if (!showRepository.existsById(id)) throw new ShowNotFoundException(id);
        showRepository.deleteById(id);
    }

    public Show buscarEntidadePorId(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
    }

    private void atualizarEntidade(Show show, ShowDTO dto) {
        show.setNome(dto.getNome());
        show.setData(dto.getData());
        // Sempre derivar ano/mes da data real para evitar dessincronia
        if (dto.getData() != null) {
            show.setAno(dto.getData().getYear());
            show.setMes(dto.getData().getMonthValue());
        } else {
            show.setAno(dto.getAno());
            show.setMes(dto.getMes());
        }
        show.setEvento(dto.getEvento());
        show.setStatus(dto.getStatus());
        show.setHoraInicio(dto.getHoraInicio());
        show.setHoraTermino(dto.getHoraTermino());
        show.setDuracao(dto.getDuracao());
        show.setCache(dto.getCache());
        show.setXdj(dto.getXdj());
        show.setAdiantamento(dto.getAdiantamento());
        show.setValorAdiantamento(dto.getValorAdiantamento());
        show.setContratante(dto.getContratante());
        show.setEndereco(dto.getEndereco());
        show.setRider(dto.getRider());
        show.setCustos(dto.getCustos());
        show.setObservacoes(dto.getObservacoes());
    }

    private Show toEntity(ShowDTO dto) {
        Show show = new Show();
        atualizarEntidade(show, dto);
        return show;
    }

    public ShowDTO toDTO(Show show) {
        ShowDTO dto = new ShowDTO();
        dto.setId(show.getId());
        dto.setNome(show.getNome());
        dto.setData(show.getData());
        dto.setAno(show.getAno());
        dto.setMes(show.getMes());
        dto.setEvento(show.getEvento());
        dto.setStatus(show.getStatus());
        dto.setHoraInicio(show.getHoraInicio());
        dto.setHoraTermino(show.getHoraTermino());
        dto.setDuracao(show.getDuracao());
        dto.setCache(show.getCache());
        dto.setXdj(show.getXdj());
        dto.setAdiantamento(show.getAdiantamento());
        dto.setValorAdiantamento(show.getValorAdiantamento());
        dto.setContratante(show.getContratante());
        dto.setEndereco(show.getEndereco());
        dto.setRider(show.getRider());
        dto.setCustos(show.getCustos());
        dto.setObservacoes(show.getObservacoes());
        return dto;
    }
}
