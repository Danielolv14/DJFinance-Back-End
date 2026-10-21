package com.druds.service;

import com.druds.model.Show;
import com.druds.service.PlanilhaShowsParser.LinhaLida;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanilhaShowsParserTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 20);

    private static List<LinhaLida> ler(String csv) throws Exception {
        return PlanilhaShowsParser.ler(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), "DRUDS", HOJE);
    }

    @Test
    void leUmaLinhaCompletaDepoisDoCabecalho() throws Exception {
        String csv = """
                cabeçalho 1
                cabeçalho 2
                cabeçalho 3
                MES,DATA,DIA,X,EVENTO,HORA,DURAÇÃO,CACHÊ,XDJ,ADIANT,VALOR ADIANT,CONTRATANTE,ENDEREÇO
                OUT,25/10/2026,SAB,,🎧 Festa X,23:00,3h,"R$ 1.500,50",TRUE,FALSE,-,Produtora Y,"Rua A, 10 — Campinas"
                """;

        List<LinhaLida> linhas = ler(csv);

        assertThat(linhas).hasSize(1);
        LinhaLida linha = linhas.get(0);
        assertThat(linha.numero()).isEqualTo(5);
        assertThat(linha.dataOriginal()).isEqualTo("25/10/2026");
        Show s = linha.show();
        assertThat(s.getDj()).isEqualTo("DRUDS");
        assertThat(s.getData()).isEqualTo(LocalDate.of(2026, 10, 25));
        assertThat(s.getMes()).isEqualTo(10);
        assertThat(s.getAno()).isEqualTo(2026);
        assertThat(s.getStatus()).isEqualTo("PENDENTE");
        assertThat(s.getEvento()).isEqualTo("Festa X");
        assertThat(s.getHoraInicio()).isEqualTo("23:00");
        assertThat(s.getDuracao()).isEqualTo("3h");
        assertThat(s.getCache()).isEqualTo(1500.50);
        assertThat(s.getXdj()).isTrue();
        assertThat(s.getAdiantamento()).isFalse();
        assertThat(s.getValorAdiantamento()).isNull();
        assertThat(s.getContratante()).isEqualTo("Produtora Y");
        assertThat(s.getEndereco()).isEqualTo("Rua A, 10 — Campinas");
    }

    @Test
    void ignoraLinhasSemDataValidaOuSemEvento() throws Exception {
        String csv = """
                1
                2
                3
                4
                OUT,a definir,,,Show sem data,,,,,,,,
                OUT,32/13/2026,,,Data inválida,,,,,,,,
                OUT,10/10/2026,,,-,,,,,,,,

                OUT,01/01/2026,,,Show passado,25h,,R$ 0,,,,a definir,
                """;

        List<LinhaLida> linhas = ler(csv);

        assertThat(linhas).hasSize(1);
        Show s = linhas.get(0).show();
        assertThat(s.getEvento()).isEqualTo("Show passado");
        assertThat(s.getStatus()).isEqualTo("CONFIRMADO");
        assertThat(s.getHoraInicio()).isNull();
        assertThat(s.getCache()).isNull();
        assertThat(s.getContratante()).isNull();
    }
}
