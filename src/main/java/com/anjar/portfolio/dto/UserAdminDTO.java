package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {

    // ===== IDENTITY =====
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String portfolioSlug;

    // ===== ROLE =====
    private String role;                    // OWNER | SUPER_ADMIN

    // ===== STATUS (BARU — Fase 6) =====
    private String status;                  // PENDING | ACTIVE | REJECTED | SUSPENDED
    private Boolean active;                 // Flag aktif lama
    private String rejectionReason;         // Alasan reject (kalau REJECTED)
    private LocalDateTime approvedAt;       // Tanggal approve
    private Long approvedBy;                // Admin yang approve

    // ===== TIMESTAMPS =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== HELPER METHODS (opsional) =====

    /**
     * Cek apakah user status PENDING.
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * Cek apakah user status ACTIVE.
     */
    public boolean isActiveStatus() {
        return "ACTIVE".equals(this.status);
    }

    /**
     * Cek apakah user status REJECTED.
     */
    public boolean isRejected() {
        return "REJECTED".equals(this.status);
    }

    /**
     * Cek apakah user status SUSPENDED.
     */
    public boolean isSuspended() {
        return "SUSPENDED".equals(this.status);
    }
}