package com.druds.controller;

import com.druds.model.BloqueioAgenda;
import com.druds.repository.BloqueioAgendaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bloqueios")
@CrossOrigin(origins = "*")
public class BloqueioAgendaController {

    private final BloqueioAgendaRepository bloqueioRepository;

    public BloqueioAgendaController(BloqueioAgendaRepository bloqueioRepository) {
        this.bloqueioRepository = bloqueioRepository;
    }

    @GetMapping
    public ResponseEntity<List<BloqueioAgenda>> listar() {
        return ResponseEntity.ok(bloqueioRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody BloqueioAgenda bloqueio) {
        if (bloqueio.getDataInicio() == null || bloqueio.getDataFim() == null) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Data início e fim são obrigatórias"));
        }
        if (bloqueio.getDataFim().isBefore(bloqueio.getDataInicio())) {
            return ResponseEntity.badRequest().body(Map.of("mensagem", "Data fim não pode ser antes da data início"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueioRepository.save(bloqueio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!bloqueioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bloqueioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
