package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.EducationDTO;
import com.anjar.portfolio.service.EducationService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller untuk CRUD Education (riwayat pendidikan user).
 *
 * Endpoint:
 *   GET    /api/me/educations           → list
 *   GET    /api/me/educations/{id}      → detail
 *   POST   /api/me/educations           → create
 *   PUT    /api/me/educations/{id}      → update
 *   DELETE /api/me/educations/{id}      → delete
 *   POST   /api/me/educations/reorder   → reorder
 */
@Slf4j
@RestController
@RequestMapping("/api/me/educations")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class EducationController {

    private final EducationService educationService;

    // ============================================================
    // LIST
    // ============================================================
    @GetMapping
    public ResponseEntity<List<EducationDTO>> list() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(educationService.listByUser(userId));
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<EducationDTO> getOne(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(educationService.getOne(userId, id));
    }

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<EducationDTO> create(
            @Valid @RequestBody EducationDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        EducationDTO created = educationService.create(userId, dto);
        return ResponseEntity.ok(created);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<EducationDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EducationDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(educationService.update(userId, id, dto));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        educationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // REORDER
    // ============================================================
    /**
     * Body: { "orderedIds": [3, 1, 2] }
     */
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody Map<String, List<Long>> body) {
        Long userId = SecurityUtil.requireCurrentUserId();
        List<Long> orderedIds = body.get("orderedIds");
        if (orderedIds == null || orderedIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        educationService.reorder(userId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}