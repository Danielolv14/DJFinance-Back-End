package com.druds.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @NotNull(message = "A data do show é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    private Integer ano;
    private Integer mes;
    private String evento;
    private String status;
    private String horaInicio;
    private String horaTermino;
    private String duracao;
    private Double cache;
    private Boolean xdj;
    private Boolean adiantamento;
    private Double valorAdiantamento;
    private String contratante;
    private String endereco;
    @Column(columnDefinition = "TEXT")
    private String rider;
    private Double custos;
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public Show() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraTermino() { return horaTermino; }
    public void setHoraTermino(String horaTermino) { this.horaTermino = horaTermino; }
    public String getDuracao() { return duracao; }
    public void setDuracao(String duracao) { this.duracao = duracao; }
    public Double getCache() { return cache; }
    public void setCache(Double cache) { this.cache = cache; }
    public Boolean getXdj() { return xdj; }
    public void setXdj(Boolean xdj) { this.xdj = xdj; }
    public Boolean getAdiantamento() { return adiantamento; }
    public void setAdiantamento(Boolean adiantamento) { this.adiantamento = adiantamento; }
    public Double getValorAdiantamento() { return valorAdiantamento; }
    public void setValorAdiantamento(Double valorAdiantamento) { this.valorAdiantamento = valorAdiantamento; }
    public String getContratante() { return contratante; }
    public void setContratante(String contratante) { this.contratante = contratante; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getRider() { return rider; }
    public void setRider(String rider) { this.rider = rider; }
    public Double getCustos() { return custos; }
    public void setCustos(Double custos) { this.custos = custos; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
