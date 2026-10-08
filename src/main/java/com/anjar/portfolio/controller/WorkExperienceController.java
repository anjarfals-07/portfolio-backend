package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.WorkExperienceDTO;
import com.anjar.portfolio.service.WorkExperienceService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller untuk CRUD WorkExperience (riwayat pekerjaan user).
 *
 * Endpoint:
 *   GET    /api/me/work-experiences           → list
 *   GET    /api/me/work-experiences/{id}      → detail
 *   POST   /api/me/work-experiences           → create
 *   PUT    /api/me/work-experiences/{id}      → update
 *   DELETE /api/me/work-experiences/{id}      → delete
 *   POST   /api/me/work-experiences/reorder   → reorder
 */
@Slf4j
@RestController
@RequestMapping("/api/me/work-experiences")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class WorkExperienceController {

    private final WorkExperienceService workService;

    // ============================================================
    // LIST
    // ============================================================
    @GetMapping
    public ResponseEntity<List<WorkExperienceDTO>> list() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(workService.listByUser(userId));
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<WorkExperienceDTO> getOne(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(workService.getOne(userId, id));
    }

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<WorkExperienceDTO> create(
            @Valid @RequestBody WorkExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(workService.create(userId, dto));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<WorkExperienceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(workService.update(userId, id, dto));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        workService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // REORDER
    // ============================================================
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorder(@RequestBody Map<String, List<Long>> body) {
        Long userId = SecurityUtil.requireCurrentUserId();
        List<Long> orderedIds = body.get("orderedIds");
        if (orderedIds == null || orderedIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        workService.reorder(userId, orderedIds);
        return ResponseEntity.noContent().build();
    }
}