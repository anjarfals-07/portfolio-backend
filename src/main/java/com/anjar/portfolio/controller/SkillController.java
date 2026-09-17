package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.SkillDTO;
import com.anjar.portfolio.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class SkillController {

    private final SkillService skillService;

    // ===== GET ALL (flat) =====
    // GET /api/skills
    @GetMapping
    public ResponseEntity<List<SkillDTO>> getAll() {
        return ResponseEntity.ok(skillService.getAll());
    }

    // ===== GET GROUPED BY CATEGORY =====
    // GET /api/skills/grouped
    // Output: { "Backend": { category, categoryIcon, items: [...] }, ... }
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, Map<String, Object>>> getGrouped() {
        return ResponseEntity.ok(skillService.getGrouped());
    }

    // ===== GET BY ID =====
    // GET /api/skills/{id}
    @GetMapping("/{id}")
    public ResponseEntity<SkillDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getById(id));
    }

    // ===== CREATE =====
    // POST /api/skills
    @PostMapping
    public ResponseEntity<SkillDTO> create(@Valid @RequestBody SkillDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skillService.create(dto));
    }

    // ===== UPDATE =====
    // PUT /api/skills/{id}
    @PutMapping("/{id}")
    public ResponseEntity<SkillDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO dto) {
        return ResponseEntity.ok(skillService.update(id, dto));
    }

    // ===== DELETE =====
    // DELETE /api/skills/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}