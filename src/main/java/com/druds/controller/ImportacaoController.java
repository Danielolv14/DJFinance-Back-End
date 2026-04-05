package com.druds.controller;

import com.druds.service.ImportacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/shows")
@CrossOrigin(origins = "*")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            var resultado = importacaoService.importarCSV(arquivo);
            return ResponseEntity.ok(Map.of(
                "importados", resultado.importados(),
                "erros",      resultado.erros(),
                "mensagem",   resultado.importados() + " shows importados com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "mensagem", "Erro ao importar: " + e.getMessage()
            ));
        }
    }
}
