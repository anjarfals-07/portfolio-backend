package com.anjar.portfolio.entity;

import com.anjar.portfolio.enums.SSLStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Custom domain per user.
 *
 * Setiap user bisa punya 0..N custom domain.
 * Salah satu bisa di-set primary.
 */
@Entity
@Table(
        name = "tenant_domains",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tenant_domains_domain",
                        columnNames = "domain"
                )
        },
        indexes = {
                @Index(name = "idx_tenant_domains_domain", columnList = "domain"),
                @Index(name = "idx_tenant_domains_user", columnList = "user_id"),
                @Index(name = "idx_tenant_domains_verified", columnList = "is_verified, domain")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // OWNER
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ============================================================
    // DOMAIN
    // ============================================================

    /**
     * Domain lowercase tanpa protocol.
     * Contoh: badru.com, portfolio.badru.com
     */
    @Column(nullable = false, unique = true, length = 255)
    private String domain;

    /**
     * Salah satu domain bisa jadi primary.
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * TRUE kalau DNS sudah menunjuk ke server kita.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * Token untuk verifikasi DNS via TXT record.
     * Format: random string 32-64 char (UUID tanpa dash).
     */
    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    /**
     * Status SSL cert.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ssl_status", nullable = false, length = 32)
    @Builder.Default
    private SSLStatus sslStatus = SSLStatus.PENDING;

    /**
     * Terakhir kali kita cek DNS.
     */
    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    /**
     * Kapan DNS berhasil diverifikasi.
     */
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // ============================================================
    // TIMESTAMPS
    // ============================================================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // HELPERS
    // ============================================================

    /**
     * Cek apakah domain sudah aktif (verified + SSL active).
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isVerified)
                && this.sslStatus == SSLStatus.ACTIVE;
    }

    /**
     * Cek apakah domain masih pending.
     */
    public boolean isPending() {
        return !Boolean.TRUE.equals(this.isVerified)
                || this.sslStatus == SSLStatus.PENDING;
    }
}