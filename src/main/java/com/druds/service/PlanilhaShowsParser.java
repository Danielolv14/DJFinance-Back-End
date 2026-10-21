package com.druds.service;

import com.druds.model.Show;
import com.druds.model.StatusShow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê a planilha de shows exportada em CSV.
 *
 * <p>Formato esperado: 4 linhas de cabeçalho e depois uma linha por show, com as colunas
 * (a partir de 0) 1 = data dd/MM/aaaa, 4 = evento, 5 = hora de início, 6 = duração,
 * 7 = cachê, 8 = XDJ, 9 = adiantamento, 10 = valor do adiantamento,
 * 11 = contratante, 12 = endereço. Linhas sem data válida ou sem evento são ignoradas.
 */
final class PlanilhaShowsParser {

    /** Show lido de uma linha da planilha, com o número da linha para mensagens de erro. */
    record LinhaLida(int numero, String dataOriginal, Show show) {}

    private static final int LINHAS_DE_CABECALHO = 4;
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PlanilhaShowsParser() {}

    static List<LinhaLida> ler(InputStream csv, String dj, LocalDate hoje) throws IOException {
        List<LinhaLida> lidas = new ArrayList<>();
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            String linha;
            int numero = 0;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (numero <= LINHAS_DE_CABECALHO || linha.isBlank()) continue;

                String[] colunas = separarColunas(linha);
                String dataOriginal = coluna(colunas, 1);
                LocalDate data = lerData(dataOriginal);
                String evento = texto(coluna(colunas, 4));
                if (data == null || evento == null) continue;

                Show show = new Show();
                show.setDj(dj);
                show.setData(data);
                show.setAno(data.getYear());
                show.setMes(data.getMonthValue());
                show.setStatus(StatusShow.inicial(data, hoje));
                show.setEvento(evento);
                show.setHoraInicio(hora(coluna(colunas, 5)));
                show.setDuracao(vazio(coluna(colunas, 6)) ? null : coluna(colunas, 6));
                show.setCache(valor(coluna(colunas, 7)));
                show.setXdj(booleano(coluna(colunas, 8)));
                show.setAdiantamento(booleano(coluna(colunas, 9)));
                show.setValorAdiantamento(valor(coluna(colunas, 10)));
                show.setContratante(texto(coluna(colunas, 11)));
                show.setEndereco(texto(coluna(colunas, 12)));
                lidas.add(new LinhaLida(numero, dataOriginal, show));
            }
        }
        return lidas;
    }

    private static LocalDate lerData(String bruto) {
        if (vazio(bruto)) return null;
        try {
            return LocalDate.parse(bruto, FORMATO_DATA);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** "R$ 1.234,56" → 1234.56. Valores vazios, inválidos ou zerados viram null. */
    private static Double valor(String bruto) {
        if (vazio(bruto)) return null;
        String numero = bruto.replaceAll("R\\$", "").replaceAll("\\s+", "").replace(".", "").replace(",", ".");
        try {
            double v = Double.parseDouble(numero);
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean booleano(String bruto) {
        return bruto.equalsIgnoreCase("TRUE");
    }

    /** Aceita só HH:mm. */
    private static String hora(String bruto) {
        return bruto.matches("\\d{2}:\\d{2}") ? bruto : null;
    }

    /** Texto sem emojis e outros símbolos; vazio vira null. */
    private static String texto(String bruto) {
        if (vazio(bruto)) return null;
        String limpo = bruto.replaceAll("[\\p{So}\\p{Cn}]", "").trim();
        return limpo.isBlank() ? null : limpo;
    }

    private static boolean vazio(String valor) {
        String t = valor.toLowerCase();
        return t.isEmpty() || t.equals("-") || t.equals("a definir");
    }

    /** Valor aparado da coluna, ou "" se a linha for mais curta. */
    private static String coluna(String[] colunas, int indice) {
        if (indice >= colunas.length || colunas[indice] == null) return "";
        return colunas[indice].trim();
    }

    /** Separa por vírgula respeitando aspas ("" dentro de aspas vira uma aspa). */
    private static String[] separarColunas(String linha) {
        List<String> colunas = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean entreAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (entreAspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    entreAspas = !entreAspas;
                }
            } else if (c == ',' && !entreAspas) {
                colunas.add(atual.toString());
                atual = new StringBuilder();
            } else {
                atual.append(c);
            }
        }
        colunas.add(atual.toString());
        return colunas.toArray(new String[0]);
    }
}
