package com.druds.controller;

import com.druds.dto.FechamentoResponseDTO;
import com.druds.service.FechamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fechamento")
@CrossOrigin(origins = "*")
public class FechamentoController {

    private final FechamentoService fechamentoService;

    public FechamentoController(FechamentoService fechamentoService) {
        this.fechamentoService = fechamentoService;
    }

    @GetMapping
    public ResponseEntity<FechamentoResponseDTO> calcular(
            @RequestParam int mes,
            @RequestParam int ano,
            @RequestParam(required = false) Double imposto) {
        return ResponseEntity.ok(fechamentoService.calcular(mes, ano, imposto));
    }
}
