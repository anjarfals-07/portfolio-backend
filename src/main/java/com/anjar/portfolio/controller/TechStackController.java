package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.TechStackDTO;
import com.anjar.portfolio.service.TechStackService;
import com.anjar.portfolio.util.SecurityUtil;
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

    @GetMapping
    public ResponseEntity<List<TechStackDTO>> getAll() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getAll(userId));
    }

    @GetMapping("/public/{portfolioSlug}")
    public ResponseEntity<List<TechStackDTO>> getPublicList(@PathVariable String portfolioSlug) {
        return ResponseEntity.ok(techStackService.getPublicList(portfolioSlug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechStackDTO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getById(id, userId));
    }

    @PostMapping
    public ResponseEntity<TechStackDTO> create(@Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(techStackService.create(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TechStackDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.update(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        techStackService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}