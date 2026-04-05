package com.druds.service;

import com.druds.model.Show;
import com.druds.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportacaoService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ShowRepository showRepository;

    public ImportacaoService(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    public ImportacaoResultado importarCSV(MultipartFile arquivo) throws Exception {
        List<Show> importados = new ArrayList<>();
        List<String> erros    = new ArrayList<>();
        int linha = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String row;
            while ((row = br.readLine()) != null) {
                linha++;
                // Pula cabeçalho e linhas vazias
                if (linha <= 4) continue;
                if (row.isBlank()) continue;

                String[] cols = parsearLinhaCSV(row);

                // Coluna 1 = data — se vazia, pula
                String rawData = get(cols, 1);
                if (vazio(rawData)) continue;

                LocalDate data = parseData(rawData);
                if (data == null) continue;

                try {
                    Show show = new Show();
                    show.setData(data);
                    show.setAno(data.getYear());

                    String mes = get(cols, 3);
                    show.setMes(mesParaNumero(mes));

                    show.setEvento(limpar(get(cols, 4)));
                    show.setHoraInicio(horaValida(get(cols, 5)));
                    show.setDuracao(duracaoValida(get(cols, 6)));
                    show.setCache(parseCache(get(cols, 7)));
                    show.setXdj(parseBoolean(get(cols, 8)));
                    show.setAdiantamento(parseBoolean(get(cols, 9)));
                    show.setValorAdiantamento(parseCache(get(cols, 10)));
                    show.setContratante(limpar(get(cols, 11)));
                    show.setEndereco(limpar(get(cols, 12)));
                    show.setStatus("CONFIRMADO");

                    // Ignora shows sem evento válido
                    if (show.getEvento() == null || show.getEvento().isBlank()) continue;

                    importados.add(show);
                } catch (Exception e) {
                    erros.add("Linha " + linha + ": " + e.getMessage());
                }
            }
        }

        showRepository.saveAll(importados);
        return new ImportacaoResultado(importados.size(), erros);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LocalDate parseData(String raw) {
        try { return LocalDate.parse(raw.trim(), FMT); }
        catch (Exception e) { return null; }
    }

    private Double parseCache(String raw) {
        if (vazio(raw)) return null;
        String limpo = raw.replaceAll("R\\$", "")
                          .replaceAll("\\s+", "")
                          .replace(".", "")
                          .replace(",", ".")
                          .trim();
        try {
            double v = Double.parseDouble(limpo);
            return v > 0 ? v : null;
        } catch (Exception e) { return null; }
    }

    private Boolean parseBoolean(String raw) {
        if (raw == null) return false;
        return raw.trim().equalsIgnoreCase("TRUE");
    }

    private String horaValida(String raw) {
        if (vazio(raw)) return null;
        String h = raw.trim();
        if (h.matches("\\d{2}:\\d{2}")) return h;
        return null;
    }

    private String duracaoValida(String raw) {
        if (vazio(raw)) return null;
        return raw.trim();
    }

    private String limpar(String raw) {
        if (vazio(raw)) return null;
        String s = raw.trim().replaceAll("[\\p{So}\\p{Cn}]", "").trim();
        return s.isBlank() ? null : s;
    }

    private boolean vazio(String v) {
        if (v == null) return true;
        String t = v.trim().toLowerCase();
        return t.isEmpty() || t.equals("-") || t.equals("a definir") || t.equals("false") && v.trim().isEmpty();
    }

    private Integer mesParaNumero(String mes) {
        if (mes == null) return null;
        return switch (mes.trim().toUpperCase()) {
            case "JANEIRO"   -> 1;  case "FEVEREIRO" -> 2;  case "MARÇO"    -> 3;
            case "ABRIL"     -> 4;  case "MAIO"       -> 5;  case "JUNHO"    -> 6;
            case "JULHO"     -> 7;  case "AGOSTO"     -> 8;  case "SETEMBRO" -> 9;
            case "OUTUBRO"   -> 10; case "NOVEMBRO"   -> 11; case "DEZEMBRO" -> 12;
            default -> null;
        };
    }

    private String get(String[] cols, int idx) {
        if (idx >= cols.length) return "";
        return cols[idx] == null ? "" : cols[idx].trim();
    }

    private String[] parsearLinhaCSV(String linha) {
        List<String> resultado = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean dentroAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (dentroAspas && i+1 < linha.length() && linha.charAt(i+1) == '"') {
                    campo.append('"'); i++;
                } else {
                    dentroAspas = !dentroAspas;
                }
            } else if (c == ',' && !dentroAspas) {
                resultado.add(campo.toString());
                campo = new StringBuilder();
            } else {
                campo.append(c);
            }
        }
        resultado.add(campo.toString());
        return resultado.toArray(new String[0]);
    }

    // ── Resultado ─────────────────────────────────────────────────────────────

    public record ImportacaoResultado(int importados, List<String> erros) {}
}
