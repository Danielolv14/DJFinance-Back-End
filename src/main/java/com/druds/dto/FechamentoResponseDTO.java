package com.druds.dto;

import java.util.List;

public class FechamentoResponseDTO {

    private Integer mes;
    private Integer ano;
    private Integer quantidadeShows;
    private Double totalBruto;
    private Double totalDaniel;
    private Double totalYuri;
    private Double totalCustos;
    private Double totalImpostos;
    private Double lucroLiquido;
    private List<ShowDTO> shows;

    public FechamentoResponseDTO() {}

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getQuantidadeShows() { return quantidadeShows; }
    public void setQuantidadeShows(Integer v) { this.quantidadeShows = v; }
    public Double getTotalBruto() { return totalBruto; }
    public void setTotalBruto(Double v) { this.totalBruto = v; }
    public Double getTotalDaniel() { return totalDaniel; }
    public void setTotalDaniel(Double v) { this.totalDaniel = v; }
    public Double getTotalYuri() { return totalYuri; }
    public void setTotalYuri(Double v) { this.totalYuri = v; }
    public Double getTotalCustos() { return totalCustos; }
    public void setTotalCustos(Double v) { this.totalCustos = v; }
    public Double getTotalImpostos() { return totalImpostos; }
    public void setTotalImpostos(Double v) { this.totalImpostos = v; }
    public Double getLucroLiquido() { return lucroLiquido; }
    public void setLucroLiquido(Double v) { this.lucroLiquido = v; }
    public List<ShowDTO> getShows() { return shows; }
    public void setShows(List<ShowDTO> shows) { this.shows = shows; }
}
