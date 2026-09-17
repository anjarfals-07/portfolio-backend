package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.TechStackDTO;
import com.anjar.portfolio.service.TechStackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tech-stack")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class TechStackController {

    private final TechStackService techStackService;

    // ===== GET ALL =====
    // GET /api/tech-stack
    @GetMapping
    public ResponseEntity<List<TechStackDTO>> getAll() {
        return ResponseEntity.ok(techStackService.getAll());
    }

    // ===== GET BY ID =====
    // GET /api/tech-stack/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TechStackDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(techStackService.getById(id));
    }

    // ===== CREATE =====
    // POST /api/tech-stack
    @PostMapping
    public ResponseEntity<TechStackDTO> create(@Valid @RequestBody TechStackDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(techStackService.create(dto));
    }

    // ===== UPDATE =====
    // PUT /api/tech-stack/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TechStackDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TechStackDTO dto) {
        return ResponseEntity.ok(techStackService.update(id, dto));
    }

    // ===== DELETE =====
    // DELETE /api/tech-stack/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        techStackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}