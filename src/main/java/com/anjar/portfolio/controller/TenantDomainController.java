package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.AddDomainRequest;
import com.anjar.portfolio.dto.TenantDomainDTO;
import com.anjar.portfolio.service.TenantDomainService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller untuk manage custom domain (owner).
 *
 * Endpoint: /api/owner/domains
 *
 * Fitur:
 * - List domain
 * - Tambah domain
 * - Verifikasi domain (cek DNS)
 * - Set primary
 * - Hapus domain
 *
 * Akses: OWNER + SUPER_ADMIN
 */
@Slf4j
@RestController
@RequestMapping("/api/owner/domains")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OWNER', 'SUPER_ADMIN')")
public class TenantDomainController {

    private final TenantDomainService domainService;

    // ============================================================
    // LIST
    // ============================================================

    /**
     * List semua domain milik user yang login.
     *
     * GET /api/owner/domains
     */
    @GetMapping
    public ResponseEntity<List<TenantDomainDTO>> listDomains() {
        Long userId = currentUserId();
        log.debug("📋 List domains for user {}", userId);
        return ResponseEntity.ok(domainService.listDomains(userId));
    }

    // ============================================================
    // ADD
    // ============================================================

    /**
     * Tambah domain baru.
     *
     * POST /api/owner/domains
     * Body: { "domain": "badru.com" }
     */
    @PostMapping
    public ResponseEntity<TenantDomainDTO> addDomain(
            @Valid @RequestBody AddDomainRequest request) {
        Long userId = currentUserId();
        log.info("➕ Add domain '{}' for user {}", request.getDomain(), userId);
        return ResponseEntity.ok(domainService.addDomain(userId, request));
    }

    // ============================================================
    // VERIFY
    // ============================================================

    /**
     * Verifikasi domain (cek DNS).
     *
     * POST /api/owner/domains/{id}/verify
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<TenantDomainDTO> verifyDomain(
            @PathVariable Long id) {
        Long userId = currentUserId();
        log.info("🔍 Verify domain id={} for user {}", id, userId);
        return ResponseEntity.ok(domainService.verifyDomain(userId, id));
    }

    // ============================================================
    // SET PRIMARY
    // ============================================================

    /**
     * Set domain sebagai primary.
     *
     * PUT /api/owner/domains/{id}/primary
     */
    @PutMapping("/{id}/primary")
    public ResponseEntity<TenantDomainDTO> setPrimary(
            @PathVariable Long id) {
        Long userId = currentUserId();
        log.info("⭐ Set primary domain id={} for user {}", id, userId);
        return ResponseEntity.ok(domainService.setPrimary(userId, id));
    }

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * Hapus domain.
     *
     * DELETE /api/owner/domains/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomain(
            @PathVariable Long id) {
        Long userId = currentUserId();
        log.info("🗑️ Delete domain id={} for user {}", id, userId);
        domainService.deleteDomain(userId, id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // HELPER
    // ============================================================

    /**
     * Extract current user ID dari SecurityContext.
     * Throw 401 kalau belum login.
     */
    private Long currentUserId() {
        return SecurityUtil.requireCurrentUserId();
    }
}