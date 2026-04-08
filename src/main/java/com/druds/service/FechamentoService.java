package com.druds.service;

import com.druds.dto.FechamentoResponseDTO;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FechamentoService {

    private static final LocalDate INICIO_EQUIPE             = LocalDate.of(2025, 3, 1);
    private static final LocalDate INICIO_PERCENTUAL_DANIEL  = LocalDate.of(2026, 1, 1);
    private static final LocalDate INICIO_PERCENTUAL_20      = LocalDate.of(2026, 4, 1);

    private static final double DANIEL_FIXO_ANTIGO     = 90.0;
    private static final double DANIEL_PERCENTUAL_15   = 0.15;
    private static final double DANIEL_PERCENTUAL_20   = 0.20;
    private static final double DANIEL_TRANSPORTE_DIA  = 40.0;
    private static final double DANIEL_SEM_CACHE       = 110.0;
    private static final double YURI_FIXO_POR_SHOW     = 300.0;

    private final ShowRepository showRepository;
    private final ShowService showService;

    public FechamentoService(ShowRepository showRepository, ShowService showService) {
        this.showRepository = showRepository;
        this.showService = showService;
    }

    public FechamentoResponseDTO calcular(int mes, int ano, Double aliquotaImposto) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Show> shows = showRepository.findByDataBetweenOrderByDataAsc(inicio, fim);

        List<Show> showsComEquipe = shows.stream()
                .filter(s -> !s.getData().isBefore(INICIO_EQUIPE))
                .toList();

        double totalBruto    = calcularTotalBruto(shows);
        double totalDaniel   = calcularTotalDaniel(showsComEquipe);
        double totalYuri     = calcularTotalYuri(showsComEquipe);
        double totalCustos   = calcularTotalCustos(shows);
        double totalImpostos = aliquotaImposto != null
                ? arredondar(totalBruto * (aliquotaImposto / 100.0))
                : 0.0;
        double lucroLiquido  = totalBruto - totalDaniel - totalYuri - totalCustos - totalImpostos;

        FechamentoResponseDTO r = new FechamentoResponseDTO();
        r.setMes(mes);
        r.setAno(ano);
        r.setQuantidadeShows(shows.size());
        r.setTotalBruto(arredondar(totalBruto));
        r.setTotalDaniel(arredondar(totalDaniel));
        r.setTotalYuri(arredondar(totalYuri));
        r.setTotalCustos(arredondar(totalCustos));
        r.setTotalImpostos(totalImpostos);
        r.setLucroLiquido(arredondar(lucroLiquido));
        r.setShows(shows.stream().map(showService::toDTO).toList());

        return r;
    }

    private double calcularTotalBruto(List<Show> shows) {
        return shows.stream()
                .mapToDouble(s -> temCache(s) ? s.getCache() : 0.0)
                .sum();
    }

    private double calcularTotalDaniel(List<Show> showsComEquipe) {
        List<Show> antigos = showsComEquipe.stream()
                .filter(s -> s.getData().isBefore(INICIO_PERCENTUAL_DANIEL)).toList();
        List<Show> novos   = showsComEquipe.stream()
                .filter(s -> !s.getData().isBefore(INICIO_PERCENTUAL_DANIEL)).toList();

        double pagAntigos = antigos.size() * DANIEL_FIXO_ANTIGO;
        double pagNovos   = novos.stream()
                .mapToDouble(s -> {
                    double pct = s.getData().isBefore(INICIO_PERCENTUAL_20)
                            ? DANIEL_PERCENTUAL_15
                            : DANIEL_PERCENTUAL_20;
                    return temCache(s) ? s.getCache() * pct : DANIEL_SEM_CACHE;
                })
                .sum();
        Set<LocalDate> dias = novos.stream().map(Show::getData).collect(Collectors.toSet());
        double transporte   = dias.size() * DANIEL_TRANSPORTE_DIA;

        return pagAntigos + pagNovos + transporte;
    }

    private double calcularTotalYuri(List<Show> showsComEquipe) {
        return showsComEquipe.size() * YURI_FIXO_POR_SHOW;
    }

    private double calcularTotalCustos(List<Show> shows) {
        return shows.stream()
                .mapToDouble(s -> s.getCustos() != null ? s.getCustos() : 0.0)
                .sum();
    }

    private boolean temCache(Show show) {
        return show.getCache() != null && show.getCache() > 0;
    }

    private double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
