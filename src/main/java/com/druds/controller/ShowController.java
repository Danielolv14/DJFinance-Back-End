package com.druds.controller;

import com.druds.dto.ShowDTO;
import com.druds.service.ShowService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shows")
@CrossOrigin(origins = "*")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @PostMapping
    public ResponseEntity<ShowDTO> criar(@Valid @RequestBody ShowDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ShowDTO dto) {
        return ResponseEntity.ok(showService.atualizar(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ShowDTO>> listarTodos() {
        return ResponseEntity.ok(showService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(showService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        showService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/duplicados")
    public ResponseEntity<Map<String, Object>> deletarDuplicados() {
        int removidos = showService.deletarDuplicados();
        return ResponseEntity.ok(Map.of(
            "removidos", removidos,
            "mensagem", removidos + " show(s) duplicado(s) removido(s)"
        ));
    }
}
