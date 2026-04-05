package com.druds.dto;

public class StatsDTO {

    private String nome;
    private int totalShows;
    private double totalFaturamento;
    private double mediaCache;
    private String ultimoShow;

    public StatsDTO(String nome, int totalShows, double totalFaturamento, double mediaCache, String ultimoShow) {
        this.nome = nome;
        this.totalShows = totalShows;
        this.totalFaturamento = totalFaturamento;
        this.mediaCache = mediaCache;
        this.ultimoShow = ultimoShow;
    }

    public String getNome() { return nome; }
    public int getTotalShows() { return totalShows; }
    public double getTotalFaturamento() { return totalFaturamento; }
    public double getMediaCache() { return mediaCache; }
    public String getUltimoShow() { return ultimoShow; }
}
