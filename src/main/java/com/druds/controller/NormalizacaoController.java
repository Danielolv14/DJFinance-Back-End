package com.druds.controller;

import com.druds.repository.ShowRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/shows")
@CrossOrigin(origins = "*")
public class NormalizacaoController {

    private final ShowRepository showRepository;

    public NormalizacaoController(ShowRepository showRepository) {
        this.showRepository = showRepository;
    }

    @PutMapping("/normalizar-local")
    @Transactional
    public ResponseEntity<Map<String, Object>> normalizarLocal(@RequestBody Map<String, String> body) {
        String de   = body.get("de");
        String para = body.get("para");

        if (de == null || para == null || de.isBlank() || para.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Campos 'de' e 'para' são obrigatórios"));
        }

        int atualizados = showRepository.atualizarEndereco(de, para);

        return ResponseEntity.ok(Map.of(
            "mensagem", atualizados + " show(s) atualizados de \"" + de + "\" para \"" + para + "\"",
            "atualizados", atualizados
        ));
    }

    @PutMapping("/normalizar-contratante")
    @Transactional
    public ResponseEntity<Map<String, Object>> normalizarContratante(@RequestBody Map<String, String> body) {
        String de   = body.get("de");
        String para = body.get("para");

        if (de == null || para == null || de.isBlank() || para.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Campos 'de' e 'para' são obrigatórios"));
        }

        int atualizados = showRepository.atualizarContratante(de, para);

        return ResponseEntity.ok(Map.of(
            "mensagem", atualizados + " show(s) atualizados de \"" + de + "\" para \"" + para + "\"",
            "atualizados", atualizados
        ));
    }
}
