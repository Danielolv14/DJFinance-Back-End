package com.druds.service;

import com.druds.dto.FechamentoResponseDTO;
import com.druds.model.Dj;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import com.druds.service.RegrasPagamento.Pagamento;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Fechamento mensal de um DJ: soma o que entrou, o que cada um recebe e o lucro. */
@Service
public class FechamentoService {

    private final ShowRepository showRepository;

    public FechamentoService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public FechamentoResponseDTO calcular(int mes, int ano, Double aliquotaImposto, String dj) {
        String djFiltro = Dj.normalizar(dj);
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim    = inicio.withDayOfMonth(inicio.lengthOfMonth());
        List<Show> shows = showRepository.findByDjAndDataBetweenOrderByDataAsc(djFiltro, inicio, fim);

        Map<Show, Pagamento> pagamentos = RegrasPagamento.calcular(shows, djFiltro);
        Collection<Pagamento> valores = pagamentos.values();

        double totalBruto    = shows.stream().mapToDouble(s -> RegrasPagamento.temCache(s) ? s.getCache() : 0.0).sum();
        double totalDaniel   = valores.stream().mapToDouble(Pagamento::daniel).sum();
        double totalYuri     = valores.stream().mapToDouble(Pagamento::yuri).sum();
        double totalCustos   = shows.stream().mapToDouble(s -> s.getCustos() != null ? s.getCustos() : 0.0).sum();
        double totalProdutor = Dj.isBraichi(djFiltro) ? totalProdutor(shows) : 0.0;
        double totalImpostos = aliquotaImposto != null ? arredondar(totalBruto * (aliquotaImposto / 100.0)) : 0.0;
        double lucroLiquido  = totalBruto - totalDaniel - totalYuri - totalCustos - totalProdutor - totalImpostos;

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
        r.setShows(shows.stream().map(ShowMapper::paraDTO).toList());
        return r;
    }

    /** Produtor externo existe só no Braichi. */
    private double totalProdutor(List<Show> shows) {
        return shows.stream()
                .filter(s -> Boolean.TRUE.equals(s.getTemProdutor()))
                .mapToDouble(s -> s.getValorProdutor() != null ? s.getValorProdutor() : 0.0)
                .sum();
    }

    private double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
