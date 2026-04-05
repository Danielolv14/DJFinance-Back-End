package com.druds.dto;

public class ProjecaoDTO {

    private int mes;
    private int ano;
    private String nomeMes;
    private double totalAgendado;
    private int quantidadeShows;
    private boolean passado;

    public ProjecaoDTO(int mes, int ano, String nomeMes, double totalAgendado, int quantidadeShows, boolean passado) {
        this.mes = mes;
        this.ano = ano;
        this.nomeMes = nomeMes;
        this.totalAgendado = totalAgendado;
        this.quantidadeShows = quantidadeShows;
        this.passado = passado;
    }

    public int getMes() { return mes; }
    public int getAno() { return ano; }
    public String getNomeMes() { return nomeMes; }
    public double getTotalAgendado() { return totalAgendado; }
    public int getQuantidadeShows() { return quantidadeShows; }
    public boolean isPassado() { return passado; }
}
