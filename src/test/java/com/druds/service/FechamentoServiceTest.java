package com.druds.service;

import com.druds.dto.FechamentoResponseDTO;
import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Trava as regras de pagamento atuais do fechamento.
 * Exemplos conferidos em "DJFinance - Regras de negócio" (segundo cérebro).
 */
class FechamentoServiceTest {

    private List<Show> showsDoMes;
    private String djConsultado;
    private FechamentoService fechamentoService;

    @BeforeEach
    void setUp() {
        // Repositório falso feito à mão: o Mockito desta versão não roda no Java 25.
        ShowRepository repositorio = (ShowRepository) Proxy.newProxyInstance(
                ShowRepository.class.getClassLoader(),
                new Class<?>[]{ShowRepository.class},
                (proxy, metodo, args) -> {
                    if (!metodo.getName().equals("findByDjAndDataBetweenOrderByDataAsc")) {
                        throw new UnsupportedOperationException(metodo.getName());
                    }
                    djConsultado = (String) args[0];
                    return showsDoMes;
                });
        fechamentoService = new FechamentoService(repositorio, new ShowService(null, null, null));
    }

    private FechamentoResponseDTO fechar(String dj, Double imposto, Show... shows) {
        showsDoMes = List.of(shows);
        FechamentoResponseDTO resultado = fechamentoService.calcular(5, 2026, imposto, dj);
        assertThat(djConsultado).isEqualTo(dj == null ? "DRUDS" : dj);
        return resultado;
    }

    private static Show show(String data, Double cache, Double custos) {
        Show s = new Show();
        s.setData(LocalDate.parse(data));
        s.setCache(cache);
        s.setCustos(custos);
        return s;
    }

    private static Show semCacheDaniel(Show s) {
        s.setSemCacheDaniel(true);
        return s;
    }

    @Nested
    class DanielDruds {

        @Test
        void antesDaEquipeNaoRecebe() {
            assertThat(fechar("DRUDS", null, show("2025-02-15", 1000.0, null)).getTotalDaniel()).isEqualTo(0.0);
        }

        @Test
        void em2025RecebeFixoDe50SemTransporte() {
            assertThat(fechar("DRUDS", null, show("2025-06-15", 1000.0, null)).getTotalDaniel()).isEqualTo(50.0);
        }

        @Test
        void deJaneiroAMarco2026Recebe10PorCentoDoLiquidoMaisTransporte() {
            // 10% de (1000 - 100) + 40
            assertThat(fechar("DRUDS", null, show("2026-02-15", 1000.0, 100.0)).getTotalDaniel()).isEqualTo(130.0);
        }

        @Test
        void aPartirDeAbril2026Recebe20PorCentoDoLiquidoMaisTransporte() {
            // 20% de (1000 - 100) + 40
            assertThat(fechar("DRUDS", null, show("2026-05-15", 1000.0, 100.0)).getTotalDaniel()).isEqualTo(220.0);
        }

        @Test
        void transporteEPorDiaNaoPorShow() {
            // 2 × (20% de 1000) + 40 uma vez só
            FechamentoResponseDTO r = fechar("DRUDS", null,
                    show("2026-05-15", 1000.0, null), show("2026-05-15", 1000.0, null));
            assertThat(r.getTotalDaniel()).isEqualTo(440.0);
        }

        @Test
        void custosMaioresQueOCacheZeramOPercentualMasMantemTransporte() {
            assertThat(fechar("DRUDS", null, show("2026-05-15", 100.0, 300.0)).getTotalDaniel()).isEqualTo(40.0);
        }

        @Test
        void showSemCachePreenchidoRecebe70MaisTransporte() {
            assertThat(fechar("DRUDS", null, show("2026-05-15", null, null)).getTotalDaniel()).isEqualTo(110.0);
            assertThat(fechar("DRUDS", null, show("2026-05-15", 0.0, null)).getTotalDaniel()).isEqualTo(110.0);
        }

        @Test
        void semCacheDanielRecebe10PorCentoDoBrutoSemTransporte() {
            assertThat(fechar("DRUDS", null, semCacheDaniel(show("2026-05-15", 1000.0, 100.0))).getTotalDaniel())
                    .isEqualTo(100.0);
        }

        @Test
        void semCacheDanielSemCachePreenchidoNaoRecebe() {
            assertThat(fechar("DRUDS", null, semCacheDaniel(show("2026-05-15", null, null))).getTotalDaniel())
                    .isEqualTo(0.0);
        }

        @Test
        void semCacheDanielEm2025TambemRecebe10PorCento() {
            assertThat(fechar("DRUDS", null, semCacheDaniel(show("2025-06-15", 1000.0, null))).getTotalDaniel())
                    .isEqualTo(100.0);
        }

        @Test
        void semCacheDanielAntesDaEquipeNaoRecebe() {
            assertThat(fechar("DRUDS", null, semCacheDaniel(show("2025-02-15", 1000.0, null))).getTotalDaniel())
                    .isEqualTo(0.0);
        }

        @Test
        void semCacheDanielNoMesmoDiaDeShowNormalMantemUmTransporte() {
            // 10% de 1000 + (20% de 1000 + 40)
            FechamentoResponseDTO r = fechar("DRUDS", null,
                    semCacheDaniel(show("2026-05-15", 1000.0, null)), show("2026-05-15", 1000.0, null));
            assertThat(r.getTotalDaniel()).isEqualTo(340.0);
        }

        @Test
        void djNuloEhTratadoComoDruds() {
            assertThat(fechar(null, null, show("2026-05-15", 1000.0, 100.0)).getTotalDaniel()).isEqualTo(220.0);
        }
    }

    @Nested
    class YuriDruds {

        @Test
        void recebe250PorShowAPartirDaEquipe() {
            FechamentoResponseDTO r = fechar("DRUDS", null,
                    show("2025-02-15", 1000.0, null), show("2025-06-15", 1000.0, null), show("2026-05-15", null, null));
            assertThat(r.getTotalYuri()).isEqualTo(500.0);
        }

        @Test
        void semCacheYuriTiraOYuriDoShow() {
            Show s = show("2026-05-15", 1000.0, null);
            s.setSemCacheYuri(true);
            assertThat(fechar("DRUDS", null, s).getTotalYuri()).isEqualTo(0.0);
        }
    }

    @Nested
    class Braichi {

        @Test
        void danielRecebe10PorCentoDoBrutoSemTransporteESemYuri() {
            FechamentoResponseDTO r = fechar("BRAICHI", null, show("2026-05-15", 1000.0, 100.0));
            assertThat(r.getTotalDaniel()).isEqualTo(100.0);
            assertThat(r.getTotalYuri()).isEqualTo(0.0);
        }

        @Test
        void semCacheDanielContinua10PorCentoDoBruto() {
            assertThat(fechar("BRAICHI", null, semCacheDaniel(show("2026-05-15", 1000.0, null))).getTotalDaniel())
                    .isEqualTo(100.0);
        }

        @Test
        void showSemCacheNaoPagaDaniel() {
            assertThat(fechar("BRAICHI", null, show("2026-05-15", null, null)).getTotalDaniel()).isEqualTo(0.0);
        }

        @Test
        void produtorExternoEntraNoFechamento() {
            Show comProdutor = show("2026-05-15", 1000.0, 100.0);
            comProdutor.setTemProdutor(true);
            comProdutor.setValorProdutor(200.0);
            Show produtorDesmarcado = show("2026-05-16", 1000.0, null);
            produtorDesmarcado.setTemProdutor(false);
            produtorDesmarcado.setValorProdutor(999.0);

            FechamentoResponseDTO r = fechar("BRAICHI", null, comProdutor, produtorDesmarcado);

            assertThat(r.getTotalProdutor()).isEqualTo(200.0);
            // 2000 bruto - 200 Daniel - 100 custos - 200 produtor
            assertThat(r.getLucroLiquido()).isEqualTo(1500.0);
        }
    }

    @Nested
    class Totais {

        @Test
        void lucroLiquidoDescontaDanielYuriCustosEImposto() {
            FechamentoResponseDTO r = fechar("DRUDS", 10.0, show("2026-05-15", 1000.0, 100.0));
            assertThat(r.getQuantidadeShows()).isEqualTo(1);
            assertThat(r.getTotalBruto()).isEqualTo(1000.0);
            assertThat(r.getTotalCustos()).isEqualTo(100.0);
            assertThat(r.getTotalImpostos()).isEqualTo(100.0);
            assertThat(r.getTotalProdutor()).isEqualTo(0.0);
            // 1000 - 220 Daniel - 250 Yuri - 100 custos - 100 imposto
            assertThat(r.getLucroLiquido()).isEqualTo(330.0);
        }

        @Test
        void produtorSoContaNoBraichi() {
            Show s = show("2026-05-15", 1000.0, null);
            s.setTemProdutor(true);
            s.setValorProdutor(200.0);
            assertThat(fechar("DRUDS", null, s).getTotalProdutor()).isEqualTo(0.0);
        }
    }
}
