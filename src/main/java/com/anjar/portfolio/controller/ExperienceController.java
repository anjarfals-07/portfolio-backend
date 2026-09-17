package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ExperienceDTO;
import com.anjar.portfolio.service.ExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ExperienceController {

    private final ExperienceService experienceService;

    // ===== GET ALL =====
    // GET /api/experiences
    @GetMapping
    public ResponseEntity<List<ExperienceDTO>> getAll() {
        return ResponseEntity.ok(experienceService.getAll());
    }

    // ===== GET BY ID =====
    // GET /api/experiences/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(experienceService.getById(id));
    }

    // ===== CREATE =====
    // POST /api/experiences
    @PostMapping
    public ResponseEntity<ExperienceDTO> create(@Valid @RequestBody ExperienceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienceService.create(dto));
    }

    // ===== UPDATE =====
    // PUT /api/experiences/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExperienceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDTO dto) {
        return ResponseEntity.ok(experienceService.update(id, dto));
    }

    // ===== DELETE =====
    // DELETE /api/experiences/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        experienceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}