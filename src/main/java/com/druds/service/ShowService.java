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

        if (show.getDj() == null || show.getDj().isBlank()) {
            show.setDj("DRUDS");
        }

        if (show.getStatus() == null || show.getStatus().isBlank()) {
            LocalDate hoje = LocalDate.now();
            show.setStatus(show.getData().isAfter(hoje) ? "PENDENTE" : "CONFIRMADO");
        }

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

    public List<ShowDTO> listarTodos(String dj) {
        String djFiltro = (dj == null || dj.isBlank()) ? "DRUDS" : dj.toUpperCase();
        LocalDate hoje = LocalDate.now();
        List<Show> shows = showRepository.findByDjOrderByDataAsc(djFiltro);

        List<Show> paraAtualizar = shows.stream()
                .filter(s -> {
                    boolean futuroConfirmado = "CONFIRMADO".equals(s.getStatus()) && s.getData().isAfter(hoje);
                    boolean passadoPendente  = "PENDENTE".equals(s.getStatus())   && !s.getData().isAfter(hoje);
                    boolean mesErrado = s.getMes() == null
                                    || s.getAno() == null
                                    || !s.getMes().equals(s.getData().getMonthValue())
                                    || !s.getAno().equals(s.getData().getYear());
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

    public int deletarDuplicados(String dj) {
        String djFiltro = (dj == null || dj.isBlank()) ? "DRUDS" : dj.toUpperCase();
        List<Show> duplicados = showRepository.findDuplicados(djFiltro);
        showRepository.deleteAll(duplicados);
        return duplicados.size();
    }

    public Show buscarEntidadePorId(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
    }

    private void atualizarEntidade(Show show, ShowDTO dto) {
        if (dto.getDj() != null && !dto.getDj().isBlank()) {
            show.setDj(dto.getDj().toUpperCase());
        } else if (show.getDj() == null) {
            show.setDj("DRUDS");
        }
        show.setNome(dto.getNome());
        show.setData(dto.getData());
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
        show.setSemCacheDaniel(dto.getSemCacheDaniel() != null ? dto.getSemCacheDaniel() : false);
        show.setSemCacheYuri(dto.getSemCacheYuri() != null ? dto.getSemCacheYuri() : false);
        show.setTemProdutor(dto.getTemProdutor() != null ? dto.getTemProdutor() : false);
        show.setValorProdutor(dto.getValorProdutor());
    }

    private Show toEntity(ShowDTO dto) {
        Show show = new Show();
        atualizarEntidade(show, dto);
        return show;
    }

    public ShowDTO toDTO(Show show) {
        ShowDTO dto = new ShowDTO();
        dto.setId(show.getId());
        dto.setDj(show.getDj() != null ? show.getDj() : "DRUDS");
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
        dto.setSemCacheDaniel(show.getSemCacheDaniel());
        dto.setSemCacheYuri(show.getSemCacheYuri());
        dto.setTemProdutor(show.getTemProdutor());
        dto.setValorProdutor(show.getValorProdutor());
        return dto;
    }
}
