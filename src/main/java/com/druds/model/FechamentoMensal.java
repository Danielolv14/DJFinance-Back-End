package com.druds.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fechamentos_mensais")
public class FechamentoMensal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "total_bruto")
    private Double totalBruto;

    @Column(name = "total_daniel")
    private Double totalDaniel;

    @Column(name = "total_yuri")
    private Double totalYuri;

    @Column(name = "lucro_liquido")
    private Double lucroLiquido;

    public FechamentoMensal() {}

    public FechamentoMensal(Integer mes, Integer ano, Double totalBruto,
                             Double totalDaniel, Double totalYuri, Double lucroLiquido) {
        this.mes = mes;
        this.ano = ano;
        this.totalBruto = totalBruto;
        this.totalDaniel = totalDaniel;
        this.totalYuri = totalYuri;
        this.lucroLiquido = lucroLiquido;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Double getTotalBruto() { return totalBruto; }
    public void setTotalBruto(Double totalBruto) { this.totalBruto = totalBruto; }

    public Double getTotalDaniel() { return totalDaniel; }
    public void setTotalDaniel(Double totalDaniel) { this.totalDaniel = totalDaniel; }

    public Double getTotalYuri() { return totalYuri; }
    public void setTotalYuri(Double totalYuri) { this.totalYuri = totalYuri; }

    public Double getLucroLiquido() { return lucroLiquido; }
    public void setLucroLiquido(Double lucroLiquido) { this.lucroLiquido = lucroLiquido; }
}
