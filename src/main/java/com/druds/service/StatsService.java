package com.druds.service;

import com.druds.dto.ProjecaoDTO;
import com.druds.dto.StatsDTO;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private static final String[] MESES = {
        "Janeiro","Fevereiro","Março","Abril","Maio","Junho",
        "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"
    };

    private final ShowRepository showRepository;

    public StatsService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    // ── CRM: stats por contratante ─────────────────────────────────────────
    public List<StatsDTO> statsPorContratante() {
        List<Show> todos = showRepository.findAll();
        return agruparPorCampo(todos, Show::getContratante);
    }

    // ── CRM: stats por local ───────────────────────────────────────────────
    public List<StatsDTO> statsPorLocal() {
        List<Show> todos = showRepository.findAll();
        return agruparPorCampo(todos, s -> s.getEndereco() != null ? s.getEndereco() : s.getContratante());
    }

    // ── Projeção: próximos 6 meses + 3 meses passados ─────────────────────
    public List<ProjecaoDTO> projecaoFaturamento() {
        List<Show> todos = showRepository.findAll();
        LocalDate hoje   = LocalDate.now();

        List<ProjecaoDTO> resultado = new ArrayList<>();

        // 3 meses passados + mês atual + 5 meses futuros = 9 meses
        for (int delta = -3; delta <= 5; delta++) {
            LocalDate ref = hoje.withDayOfMonth(1).plusMonths(delta);
            int mes = ref.getMonthValue();
            int ano = ref.getYear();

            List<Show> showsDoMes = todos.stream()
                    .filter(s -> s.getMes() != null && s.getAno() != null)
                    .filter(s -> s.getMes() == mes && s.getAno() == ano)
                    .filter(s -> !"CANCELADO".equals(s.getStatus()))
                    .toList();

            double total = showsDoMes.stream()
                    .mapToDouble(s -> s.getCache() != null && s.getCache() > 0 ? s.getCache() : 0.0)
                    .sum();

            resultado.add(new ProjecaoDTO(
                mes, ano, MESES[mes-1] + "/" + ano,
                Math.round(total * 100.0) / 100.0,
                showsDoMes.size(),
                ref.isBefore(hoje.withDayOfMonth(1))
            ));
        }

        return resultado;
    }

    // ── Helper: agrupa shows por um campo string ───────────────────────────
    private List<StatsDTO> agruparPorCampo(List<Show> todos, java.util.function.Function<Show,String> campo) {
        Map<String, List<Show>> agrupado = todos.stream()
                .filter(s -> campo.apply(s) != null && !campo.apply(s).isBlank())
                .collect(Collectors.groupingBy(campo));

        return agrupado.entrySet().stream()
                .map(entry -> {
                    List<Show> lista = entry.getValue();
                    double total = lista.stream()
                            .mapToDouble(s -> s.getCache() != null && s.getCache() > 0 ? s.getCache() : 0.0)
                            .sum();
                    double media = lista.isEmpty() ? 0 : total / lista.size();
                    String ultimo = lista.stream()
                            .map(Show::getData)
                            .filter(Objects::nonNull)
                            .max(Comparator.naturalOrder())
                            .map(d -> d.getDayOfMonth() + "/" + d.getMonthValue() + "/" + d.getYear())
                            .orElse("—");
                    return new StatsDTO(entry.getKey(), lista.size(),
                            Math.round(total*100.0)/100.0,
                            Math.round(media*100.0)/100.0, ultimo);
                })
                .sorted(Comparator.comparingInt(StatsDTO::getTotalShows).reversed())
                .toList();
    }
}
