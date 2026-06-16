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

    private static final LocalDate INICIO_EQUIPE            = LocalDate.of(2025, 3, 1);
    private static final LocalDate INICIO_PERCENTUAL_DANIEL = LocalDate.of(2026, 1, 1);
    private static final LocalDate INICIO_PERCENTUAL_20     = LocalDate.of(2026, 4, 1);

    private static final double DANIEL_FIXO_ANTIGO    = 50.0;
    private static final double DANIEL_PERCENTUAL_10  = 0.10;
    private static final double DANIEL_PERCENTUAL_20  = 0.20;
    private static final double DANIEL_TRANSPORTE_DIA = 40.0;
    private static final double DANIEL_SEM_CACHE      = 110.0;
    private static final double YURI_FIXO_POR_SHOW    = 300.0;

    private final ShowRepository showRepository;
    private final ShowService showService;

    public FechamentoService(ShowRepository showRepository, ShowService showService) {
        this.showRepository = showRepository;
        this.showService = showService;
    }

    public FechamentoResponseDTO calcular(int mes, int ano, Double aliquotaImposto, String dj) {
        String djFiltro = (dj == null || dj.isBlank()) ? "DRUDS" : dj.toUpperCase();
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Show> shows = showRepository.findByDjAndDataBetweenOrderByDataAsc(djFiltro, inicio, fim);

        List<Show> showsParaDaniel = shows.stream()
                .filter(s -> s.getSemCacheDaniel() == null || !s.getSemCacheDaniel())
                .toList();

        double totalBruto    = calcularTotalBruto(shows);
        double totalDaniel   = "BRAICHI".equals(djFiltro)
                ? calcularDanielBraichi(showsParaDaniel)
                : calcularDanielDruds(showsParaDaniel);
        double totalYuri     = "BRAICHI".equals(djFiltro)
                ? 0.0
                : calcularYuriDruds(shows.stream()
                        .filter(s -> !s.getData().isBefore(INICIO_EQUIPE))
                        .filter(s -> s.getSemCacheYuri() == null || !s.getSemCacheYuri())
                        .toList());
        double totalCustos    = calcularTotalCustos(shows);
        double totalProdutor  = "BRAICHI".equals(djFiltro) ? calcularTotalProdutor(shows) : 0.0;
        double totalImpostos  = aliquotaImposto != null
                ? arredondar(totalBruto * (aliquotaImposto / 100.0))
                : 0.0;
        double lucroLiquido   = totalBruto - totalDaniel - totalYuri - totalCustos - totalProdutor - totalImpostos;

        FechamentoResponseDTO r = new FechamentoResponseDTO();
        r.setMes(mes);
        r.setAno(ano);
        r.setQuantidadeShows(shows.size());
        r.setTotalBruto(arredondar(totalBruto));
        r.setTotalDaniel(arredondar(totalDaniel));
        r.setTotalYuri(arredondar(totalYuri));
        r.setTotalCustos(arredondar(totalCustos));
        r.setTotalImpostos(totalImpostos);
        r.setTotalProdutor(arredondar(totalProdutor));
        r.setLucroLiquido(arredondar(lucroLiquido));
        r.setShows(shows.stream().map(showService::toDTO).toList());

        return r;
    }

    private double calcularTotalBruto(List<Show> shows) {
        return shows.stream()
                .mapToDouble(s -> temCache(s) ? s.getCache() : 0.0)
                .sum();
    }

    // ── DRUDS: R$50 fixo (mar-dez 2025), 10% (jan-mar 2026), 20% (abr 2026+) + transporte ──
    private double calcularDanielDruds(List<Show> showsComEquipe) {
        List<Show> comEquipe = showsComEquipe.stream()
                .filter(s -> !s.getData().isBefore(INICIO_EQUIPE)).toList();
        List<Show> antigos = comEquipe.stream()
                .filter(s -> s.getData().isBefore(INICIO_PERCENTUAL_DANIEL)).toList();
        List<Show> novos   = comEquipe.stream()
                .filter(s -> !s.getData().isBefore(INICIO_PERCENTUAL_DANIEL)).toList();

        double pagAntigos = antigos.size() * DANIEL_FIXO_ANTIGO;
        double pagNovos   = novos.stream()
                .mapToDouble(s -> {
                    if (!temCache(s)) return DANIEL_SEM_CACHE;
                    double pct = s.getData().isBefore(INICIO_PERCENTUAL_20)
                            ? DANIEL_PERCENTUAL_10
                            : DANIEL_PERCENTUAL_20;
                    double custos = s.getCustos() != null ? s.getCustos() : 0.0;
                    double base   = s.getCache() - custos;
                    return base > 0 ? base * pct : 0.0;
                })
                .sum();
        Set<LocalDate> dias = novos.stream().map(Show::getData).collect(Collectors.toSet());
        double transporte   = dias.size() * DANIEL_TRANSPORTE_DIA;

        return pagAntigos + pagNovos + transporte;
    }

    // ── BRAICHI: 10% do cachê bruto de cada show ──
    private double calcularDanielBraichi(List<Show> shows) {
        return shows.stream()
                .mapToDouble(s -> temCache(s) ? s.getCache() * DANIEL_PERCENTUAL_10 : 0.0)
                .sum();
    }

    private double calcularYuriDruds(List<Show> showsComEquipe) {
        return showsComEquipe.size() * YURI_FIXO_POR_SHOW;
    }

    private double calcularTotalCustos(List<Show> shows) {
        return shows.stream()
                .mapToDouble(s -> s.getCustos() != null ? s.getCustos() : 0.0)
                .sum();
    }

    private double calcularTotalProdutor(List<Show> shows) {
        return shows.stream()
                .filter(s -> Boolean.TRUE.equals(s.getTemProdutor()))
                .mapToDouble(s -> s.getValorProdutor() != null ? s.getValorProdutor() : 0.0)
                .sum();
    }

    private boolean temCache(Show show) {
        return show.getCache() != null && show.getCache() > 0;
    }

    private double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
