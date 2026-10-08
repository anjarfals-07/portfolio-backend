package com.anjar.portfolio.dto;

import com.anjar.portfolio.enums.SSLStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO untuk custom domain.
 *
 * Include DNS instructions supaya frontend bisa tampilkan
 * panduan ke user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDomainDTO {

    // ============================================================
    // BASIC
    // ============================================================

    private Long id;
    private String domain;
    private Boolean isPrimary;
    private Boolean isVerified;
    private SSLStatus sslStatus;

    // ============================================================
    // TIMESTAMPS
    // ============================================================

    private LocalDateTime lastCheckedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    // ============================================================
    // DNS INSTRUCTIONS (untuk frontend tampilkan)
    // ============================================================

    /**
     * Token verifikasi (untuk TXT record).
     * Contoh: abc123xyz456
     */
    private String verificationToken;

    /**
     * Target CNAME yang perlu di-set user.
     * Contoh: app.platform.com
     */
    private String cnameTarget;

    /**
     * TXT record name untuk verifikasi.
     * Contoh: _platform-verify.badru.com
     */
    private String txtRecordName;

    /**
     * TXT record value.
     * Contoh: platform-verify=abc123xyz
     */
    private String txtRecordValue;
}