package com.anjar.portfolio.entity;

import com.anjar.portfolio.enums.ThemeLayout;
import com.anjar.portfolio.enums.ThemeMode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Theme entity — custom theme per user.
 *
 * Setiap user bisa punya 1 theme (OneToOne dengan User).
 * Theme ini nyimpen:
 * - Warna (primary, accent, bg, text)
 * - Typography (font body, font heading)
 * - Layout (grid, list, compact)
 * - Border radius
 * - Icon logo
 * - Default mode (light/dark)
 * - Nama preset (kalau pakai preset)
 *
 * Semua field nullable — kalau null, frontend pakai default CSS variable.
 */
@Entity
@Table(
        name = "themes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_themes_user", columnNames = "user_id")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // OWNER — OneToOne ke User
    // ============================================================

    /**
     * User pemilik theme ini.
     * OneToOne — 1 user cuma punya 1 theme.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ============================================================
    // COLORS
    // ============================================================

    /**
     * Warna utama — buat button, link, aksen.
     * Contoh: #3b82f6
     */
    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    /**
     * Warna aksen — buat gradient, highlight.
     * Contoh: #8b5cf6
     */
    @Column(name = "accent_color", length = 20)
    private String accentColor;

    /**
     * Warna background utama.
     * Contoh: #ffffff
     */
    @Column(name = "bg_color", length = 20)
    private String bgColor;

    /**
     * Warna text utama.
     * Contoh: #1e293b
     */
    @Column(name = "text_color", length = 20)
    private String textColor;

    // ============================================================
    // TYPOGRAPHY
    // ============================================================

    /**
     * Font buat body text.
     * Contoh: 'Inter', sans-serif
     */
    @Column(name = "font_family", length = 100)
    private String fontFamily;

    /**
     * Font buat heading (h1, h2, dll).
     * Contoh: 'Poppins', sans-serif
     */
    @Column(name = "heading_font", length = 100)
    private String headingFont;

    // ============================================================
    // LAYOUT
    // ============================================================

    /**
     * Border radius — buat card, button, input.
     * Contoh: 12px, 16px
     */
    @Column(name = "border_radius", length = 20)
    private String borderRadius;

    /**
     * Layout style untuk halaman projects/blog.
     * GRID | LIST | COMPACT
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ThemeLayout layout = ThemeLayout.GRID;

    // ============================================================
    // IDENTITY
    // ============================================================

    /**
     * Icon logo — pakai PrimeIcons.
     * Contoh: pi pi-code, pi pi-sparkles
     */
    @Column(name = "logo_icon", length = 100)
    @Builder.Default
    private String logoIcon = "pi pi-code";

    /**
     * Default mode saat user buka portfolio.
     * LIGHT | DARK
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_mode", length = 20)
    @Builder.Default
    private ThemeMode defaultMode = ThemeMode.LIGHT;

    /**
     * Nama preset yang dipakai (kalau pakai preset).
     * Contoh: modern-blue, emerald-fresh
     */
    @Column(length = 50)
    private String preset;

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
    // HELPER METHODS
    // ============================================================

    /**
     * Cek apakah theme ini pakai preset.
     */
    public boolean isUsingPreset() {
        return this.preset != null && !this.preset.isBlank();
    }

    /**
     * Cek apakah user customize warna (bukan preset).
     */
    public boolean isCustomTheme() {
        return this.primaryColor != null || this.accentColor != null;
    }
}