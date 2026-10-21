package com.druds.service;

import com.druds.model.Dj;
import com.druds.model.Show;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quanto o Daniel e o Yuri recebem em cada show.
 *
 * <p>É a única fonte dessas regras no sistema: o fechamento soma estes valores
 * e o front só exibe o que vem da API. Exemplos conferidos à mão estão em
 * {@code FechamentoServiceTest}.
 *
 * <h2>DRUDS</h2>
 * <ul>
 *   <li>Antes de mar/2025 não havia equipe: ninguém recebe.</li>
 *   <li>"Sem cachê Daniel" marcado: 10% do cachê bruto, sem transporte.</li>
 *   <li>mar/2025 a dez/2025: R$ 50 fixos por show.</li>
 *   <li>A partir de jan/2026: show sem cachê preenchido paga R$ 70; com cachê,
 *       10% (jan a mar/2026) ou 20% (abr/2026 em diante) de cachê − custos.</li>
 *   <li>A partir de jan/2026, + R$ 40 de transporte por <b>dia</b> com show,
 *       somado ao primeiro show daquele dia.</li>
 *   <li>Yuri: R$ 250 por show a partir de mar/2025, salvo "Sem cachê Yuri".</li>
 * </ul>
 *
 * <h2>BRAICHI</h2>
 * Daniel recebe 10% do cachê bruto de todo show; não há Yuri nem transporte.
 */
public final class RegrasPagamento {

    /** Valores de um show. */
    public record Pagamento(double daniel, double yuri) {}

    static final LocalDate INICIO_EQUIPE        = LocalDate.of(2025, 3, 1);
    static final LocalDate INICIO_PERCENTUAL    = LocalDate.of(2026, 1, 1);
    static final LocalDate INICIO_PERCENTUAL_20 = LocalDate.of(2026, 4, 1);

    static final double DANIEL_FIXO_2025          = 50.0;
    static final double DANIEL_SEM_CACHE_DEFINIDO = 70.0;
    static final double PERCENTUAL_10             = 0.10;
    static final double PERCENTUAL_20             = 0.20;
    static final double TRANSPORTE_POR_DIA        = 40.0;
    static final double YURI_POR_SHOW             = 250.0;

    private RegrasPagamento() {}

    /** Pagamentos de cada show da lista. Os shows devem ser todos do mesmo DJ. */
    public static Map<Show, Pagamento> calcular(List<Show> shows, String dj) {
        boolean braichi = Dj.isBraichi(dj);
        Map<Show, Pagamento> pagamentos = new IdentityHashMap<>();
        Set<LocalDate> diasComTransporte = new HashSet<>();

        for (Show show : shows) {
            double daniel = braichi ? dezPorCentoDoBruto(show) : danielDruds(show);
            if (!braichi && pagaTransporte(show) && diasComTransporte.add(show.getData())) {
                daniel += TRANSPORTE_POR_DIA;
            }
            double yuri = braichi ? 0.0 : yuri(show);
            pagamentos.put(show, new Pagamento(daniel, yuri));
        }
        return pagamentos;
    }

    private static double danielDruds(Show show) {
        LocalDate data = show.getData();
        if (data.isBefore(INICIO_EQUIPE)) return 0.0;
        if (semCacheDaniel(show))         return dezPorCentoDoBruto(show);
        if (data.isBefore(INICIO_PERCENTUAL)) return DANIEL_FIXO_2025;
        if (!temCache(show))              return DANIEL_SEM_CACHE_DEFINIDO;

        double percentual = data.isBefore(INICIO_PERCENTUAL_20) ? PERCENTUAL_10 : PERCENTUAL_20;
        double custos = show.getCustos() != null ? show.getCustos() : 0.0;
        double base = show.getCache() - custos;
        return base > 0 ? base * percentual : 0.0;
    }

    private static boolean pagaTransporte(Show show) {
        return !show.getData().isBefore(INICIO_PERCENTUAL) && !semCacheDaniel(show);
    }

    private static double yuri(Show show) {
        boolean naEquipe = !show.getData().isBefore(INICIO_EQUIPE);
        return naEquipe && !Boolean.TRUE.equals(show.getSemCacheYuri()) ? YURI_POR_SHOW : 0.0;
    }

    private static double dezPorCentoDoBruto(Show show) {
        return temCache(show) ? show.getCache() * PERCENTUAL_10 : 0.0;
    }

    private static boolean semCacheDaniel(Show show) {
        return Boolean.TRUE.equals(show.getSemCacheDaniel());
    }

    static boolean temCache(Show show) {
        return show.getCache() != null && show.getCache() > 0;
    }
}
