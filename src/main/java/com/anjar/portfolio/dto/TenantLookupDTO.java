package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response untuk /api/public/tenant-by-domain.
 *
 * Dipakai frontend untuk deteksi tenant dari hostname.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLookupDTO {

    /**
     * Username user (portfolio slug).
     * Contoh: "badru"
     */
    private String username;

    /**
     * Display name user (untuk white-label).
     * Contoh: "Badru Pratama"
     */
    private String displayName;
}