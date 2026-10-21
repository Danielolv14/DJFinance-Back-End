package com.druds.controller;

import com.druds.service.NormalizacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shows")
public class NormalizacaoController {

    private final NormalizacaoService normalizacaoService;

    public NormalizacaoController(NormalizacaoService normalizacaoService) {
        this.normalizacaoService = normalizacaoService;
    }

    @PutMapping("/normalizar-local")
    public ResponseEntity<Map<String, Object>> normalizarLocal(
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "DRUDS") String dj) {
        int atualizados = normalizacaoService.renomearLocal(body.get("de"), body.get("para"), dj);
        return ResponseEntity.ok(resposta(atualizados, body));
    }

    @PutMapping("/normalizar-contratante")
    public ResponseEntity<Map<String, Object>> normalizarContratante(
            @RequestBody Map<String, String> body,
            @RequestParam(defaultValue = "DRUDS") String dj) {
        int atualizados = normalizacaoService.renomearContratante(body.get("de"), body.get("para"), dj);
        return ResponseEntity.ok(resposta(atualizados, body));
    }

    private Map<String, Object> resposta(int atualizados, Map<String, String> body) {
        return Map.of(
            "mensagem", atualizados + " show(s) atualizados de \"" + body.get("de") + "\" para \"" + body.get("para") + "\"",
            "atualizados", atualizados
        );
    }
}
