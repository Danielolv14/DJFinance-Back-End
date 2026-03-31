package com.druds.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FechamentoMensal {

    private final String mes;
    private final int ano;
    private final List<Show> shows;

    private double totalBruto;
    private int quantidadeShowsSemValor;
    private double totalCustos;
    private double lucroLiquido;

    private final Map<String, Double> pagamentosPorPessoa = new LinkedHashMap<>();
    private final Map<String, Integer> showsPorPessoa = new LinkedHashMap<>();

    public FechamentoMensal(String mes, int ano, List<Show> shows) {
        this.mes = mes;
        this.ano = ano;
        this.shows = shows;
    }

    public void adicionarPagamento(String nomePessoa, double valor, int qtdShows) {
        pagamentosPorPessoa.merge(nomePessoa, valor, Double::sum);
        showsPorPessoa.merge(nomePessoa, qtdShows, Integer::sum);
    }

    public String getMes() { return mes; }
    public int getAno() { return ano; }
    public List<Show> getShows() { return shows; }
    public int getQuantidadeShows() { return shows.size(); }

    public double getTotalBruto() { return totalBruto; }
    public void setTotalBruto(double v) { this.totalBruto = v; }

    public int getQuantidadeShowsSemValor() { return quantidadeShowsSemValor; }
    public void setQuantidadeShowsSemValor(int v) { this.quantidadeShowsSemValor = v; }

    public double getTotalCustos() { return totalCustos; }
    public void setTotalCustos(double v) { this.totalCustos = v; }

    public double getLucroLiquido() { return lucroLiquido; }
    public void setLucroLiquido(double v) { this.lucroLiquido = v; }

    public Map<String, Double> getPagamentosPorPessoa() { return pagamentosPorPessoa; }
    public Map<String, Integer> getShowsPorPessoa() { return showsPorPessoa; }
}
