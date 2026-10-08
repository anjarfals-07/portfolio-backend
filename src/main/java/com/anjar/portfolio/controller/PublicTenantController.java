package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.TenantLookupDTO;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.service.TenantDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantDomainService domainService;

    /**
     * Lookup tenant by domain.
     *
     * Dipakai frontend untuk:
     * - Deteksi tenant dari hostname (badru.com → username: badru)
     * - Render portfolio user yang sesuai
     *
     * Endpoint: GET /api/public/tenant-by-domain?domain=badru.com
     * Response: { "username": "badru", "displayName": "Badru" }
     */
    @GetMapping("/tenant-by-domain")
    public ResponseEntity<?> getTenantByDomain(@RequestParam String domain) {
        log.debug("🔍 Tenant lookup: {}", domain);

        return domainService.lookupByDomain(domain)
                .map(user -> ResponseEntity.ok(toDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ============================================================
    // HELPER
    // ============================================================

    private TenantLookupDTO toDTO(User user) {
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isBlank()) {
            displayName = user.getUsername();
        }

        return TenantLookupDTO.builder()
                .username(user.getUsername())
                .displayName(displayName)
                .build();
    }
}