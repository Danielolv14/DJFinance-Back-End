package com.druds.controller;

import com.druds.model.BloqueioAgenda;
import com.druds.service.BloqueioAgendaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bloqueios")
public class BloqueioAgendaController {

    private final BloqueioAgendaService bloqueioService;

    public BloqueioAgendaController(BloqueioAgendaService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    @GetMapping
    public ResponseEntity<List<BloqueioAgenda>> listar(
            @RequestParam(required = false, defaultValue = "DRUDS") String dj) {
        return ResponseEntity.ok(bloqueioService.listar(dj));
    }

    @PostMapping
    public ResponseEntity<BloqueioAgenda> criar(
            @RequestParam(required = false, defaultValue = "DRUDS") String dj,
            @RequestBody BloqueioAgenda bloqueio) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueioService.criar(dj, bloqueio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return bloqueioService.deletar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
