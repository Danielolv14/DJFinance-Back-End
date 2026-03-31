package com.druds.model;

import java.time.LocalDate;

public class Show {

    public static final double CACHE_PADRAO = 110.0;

    private LocalDate data;
    private int ano;
    private String mes;
    private String evento;
    private String horaInicio;
    private String duracaoShow;
    private Double cache;
    private String xdj;
    private Boolean adiantamento;
    private Double valorAdiantamento;
    private String contratante;
    private String endereco;

    public Show() {}

    public double getCacheEfetivo() {
        return (cache == null || cache <= 0) ? CACHE_PADRAO : cache;
    }

    public boolean isCacheDefinido() {
        return cache != null && cache > 0;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getDuracaoShow() { return duracaoShow; }
    public void setDuracaoShow(String duracaoShow) { this.duracaoShow = duracaoShow; }

    public Double getCache() { return cache; }
    public void setCache(Double cache) { this.cache = cache; }

    public String getXdj() { return xdj; }
    public void setXdj(String xdj) { this.xdj = xdj; }

    public Boolean getAdiantamento() { return adiantamento; }
    public void setAdiantamento(Boolean adiantamento) { this.adiantamento = adiantamento; }

    public Double getValorAdiantamento() { return valorAdiantamento; }
    public void setValorAdiantamento(Double valorAdiantamento) { this.valorAdiantamento = valorAdiantamento; }

    public String getContratante() { return contratante; }
    public void setContratante(String contratante) { this.contratante = contratante; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        return String.format("[%s] %s | Cachê: R$ %.2f | Contratante: %s",
                data != null ? data.toString() : "??",
                evento,
                getCacheEfetivo(),
                contratante);
    }
}
