package com.druds.controller;

import com.druds.dto.ProjecaoDTO;
import com.druds.dto.StatsDTO;
import com.druds.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/contratantes")
    public ResponseEntity<List<StatsDTO>> contratantes() {
        return ResponseEntity.ok(statsService.statsPorContratante());
    }

    @GetMapping("/locais")
    public ResponseEntity<List<StatsDTO>> locais() {
        return ResponseEntity.ok(statsService.statsPorLocal());
    }

    @GetMapping("/projecao")
    public ResponseEntity<List<ProjecaoDTO>> projecao() {
        return ResponseEntity.ok(statsService.projecaoFaturamento());
    }
}
