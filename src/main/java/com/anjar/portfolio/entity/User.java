package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User entity — dipakai untuk auth & multi-tenant.
 *
 * Setiap user punya:
 * - username: untuk login
 * - portfolioSlug: URL unik untuk portfolio mereka (/anjar, /budi, dll)
 * - role: OWNER atau SUPER_ADMIN
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_portfolio_slug", columnNames = "portfolio_slug")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== AUTH =====

    /**
     * Username untuk login (unique, lowercase)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Password (BCrypt hashed)
     */
    @Column(nullable = false)
    private String password;

    // ===== MULTI-TENANT =====

    /**
     * Slug URL portfolio user: anjar.dev/portfolio → slug "portfolio"
     *
     * Atau kalau pakai subdomain: portfolio.anjar.dev → slug "anjar"
     *
     * Wajib unique. Gak boleh sama dengan ReservedSlugs.
     */
    @Column(name = "portfolio_slug", nullable = false, unique = true, length = 50)
    private String portfolioSlug;

    /**
     * Role user: OWNER (user biasa) atau SUPER_ADMIN (kamu)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.OWNER;

    // ===== PROFILE INFO =====

    /**
     * Email untuk kontak & notifikasi
     */
    @Column(nullable = false, length = 200)
    private String email;

    /**
     * Nama tampilan (buat header "Hi, Anjar")
     */
    @Column(name = "display_name", length = 100)
    private String displayName;

    // ===== STATUS =====

    /**
     * Status aktif. Kalau false, user gak bisa login.
     * Super admin bisa disable user (misal spam).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ===== TIMESTAMPS =====

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== HELPER METHODS =====

    /**
     * Cek apakah user super admin.
     */
    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }

    /**
     * Cek apakah user owner.
     */
    public boolean isOwner() {
        return this.role == UserRole.OWNER;
    }
}