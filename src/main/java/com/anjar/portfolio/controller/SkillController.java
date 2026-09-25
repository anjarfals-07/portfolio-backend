package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.SkillDTO;
import com.anjar.portfolio.service.SkillService;
import com.anjar.portfolio.util.SecurityUtil;
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

    @GetMapping
    public ResponseEntity<List<SkillDTO>> getAll() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getAll(userId));
    }

    @GetMapping("/grouped")
    public ResponseEntity<Map<String, Map<String, Object>>> getGrouped() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getGrouped(userId));
    }

    @GetMapping("/public/{portfolioSlug}")
    public ResponseEntity<Map<String, Map<String, Object>>> getPublicGrouped(
            @PathVariable String portfolioSlug) {
        return ResponseEntity.ok(skillService.getPublicGrouped(portfolioSlug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillDTO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getById(id, userId));
    }

    @PostMapping
    public ResponseEntity<SkillDTO> create(@Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skillService.create(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.update(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        skillService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}