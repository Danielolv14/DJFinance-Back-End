package com.druds.service;

import com.druds.dto.ShowDTO;
import com.druds.model.Dj;
import com.druds.model.Show;

/** Converte entre a entidade {@link Show} e o {@link ShowDTO} que trafega na API. */
final class ShowMapper {

    private ShowMapper() {}

    /** Copia os campos editáveis do DTO para a entidade (nova ou existente). */
    static void copiarParaEntidade(ShowDTO dto, Show show) {
        if (dto.getDj() != null && !dto.getDj().isBlank()) {
            show.setDj(Dj.normalizar(dto.getDj()));
        } else if (show.getDj() == null) {
            show.setDj(Dj.DRUDS);
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
        show.setSemCacheDaniel(Boolean.TRUE.equals(dto.getSemCacheDaniel()));
        show.setSemCacheYuri(Boolean.TRUE.equals(dto.getSemCacheYuri()));
        show.setTemProdutor(Boolean.TRUE.equals(dto.getTemProdutor()));
        show.setValorProdutor(dto.getValorProdutor());
    }

    static ShowDTO paraDTO(Show show) {
        ShowDTO dto = new ShowDTO();
        dto.setId(show.getId());
        dto.setDj(show.getDj() != null ? show.getDj() : Dj.DRUDS);
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
        dto.setGoogleEventId(show.getGoogleEventId());
        return dto;
    }
}
