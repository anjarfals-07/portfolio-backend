package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ExperienceDTO;
import com.anjar.portfolio.service.ExperienceService;
import com.anjar.portfolio.util.SecurityUtil;
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

    @GetMapping
    public ResponseEntity<List<ExperienceDTO>> getAll() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getAll(userId));
    }

    @GetMapping("/public/{portfolioSlug}")
    public ResponseEntity<List<ExperienceDTO>> getPublicList(@PathVariable String portfolioSlug) {
        return ResponseEntity.ok(experienceService.getPublicList(portfolioSlug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperienceDTO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getById(id, userId));
    }

    @PostMapping
    public ResponseEntity<ExperienceDTO> create(@Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienceService.create(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExperienceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.update(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        experienceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}