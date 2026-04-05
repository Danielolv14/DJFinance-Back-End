package com.druds.service;

import com.druds.dto.ShowDTO;
import com.druds.exception.ShowNotFoundException;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowService {

    private final ShowRepository showRepository;

    public ShowService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public ShowDTO criar(ShowDTO dto) {
        return toDTO(showRepository.save(toEntity(dto)));
    }

    public ShowDTO atualizar(Long id, ShowDTO dto) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        atualizarEntidade(show, dto);
        return toDTO(showRepository.save(show));
    }

    public List<ShowDTO> listarTodos() {
        return showRepository.findAll().stream().map(this::toDTO).toList();
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
        show.setAno(dto.getAno());
        show.setMes(dto.getMes());
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
