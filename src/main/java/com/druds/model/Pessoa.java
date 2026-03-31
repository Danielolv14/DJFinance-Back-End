package com.druds.model;

public class Pessoa {

    public enum TipoPagamento {
        PERCENTUAL,
        FIXO
    }

    private final String nome;
    private final TipoPagamento tipoPagamento;
    private final double valorOuPercentual;
    private final double transportePorShow;

    public Pessoa(String nome, TipoPagamento tipoPagamento, double valorOuPercentual, double transportePorShow) {
        this.nome = nome;
        this.tipoPagamento = tipoPagamento;
        this.valorOuPercentual = valorOuPercentual;
        this.transportePorShow = transportePorShow;
    }

    public double calcularPagamentoPorShow(Show show) {
        double base = tipoPagamento == TipoPagamento.PERCENTUAL
                ? show.getCacheEfetivo() * valorOuPercentual
                : valorOuPercentual;
        return base + transportePorShow;
    }

    public String getNome() { return nome; }
    public TipoPagamento getTipoPagamento() { return tipoPagamento; }
    public double getValorOuPercentual() { return valorOuPercentual; }
    public double getTransportePorShow() { return transportePorShow; }

    @Override
    public String toString() {
        if (tipoPagamento == TipoPagamento.PERCENTUAL) {
            return String.format("%s (%.0f%% do cachê + R$ %.2f transporte/show)", nome, valorOuPercentual * 100, transportePorShow);
        }
        return String.format("%s (R$ %.2f fixo/show)", nome, valorOuPercentual);
    }
}
