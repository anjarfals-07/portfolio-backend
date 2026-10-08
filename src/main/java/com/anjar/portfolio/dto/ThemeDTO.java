package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO untuk theme.
 *
 * Dipakai di:
 * - GET /api/themes/me (response)
 * - PUT /api/themes/me (request)
 * - GET /api/users/{username}/theme (response)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeDTO {

    private Long id;

    // ============================================================
    // ⭐ OWNER INFO — untuk white-label
    // ============================================================

    /**
     * Username user (dari User.username).
     * Diisi backend saat response public theme.
     * Null saat request save theme (frontend tidak kirim).
     */
    private String username;

    /**
     * Display name user (dari User.displayName).
     * Dipakai frontend untuk white-label (navbar, footer, meta tag).
     * Diisi backend saat response public theme.
     * Null saat request save theme (frontend tidak kirim).
     */
    private String displayName;

    // ============================================================
    // COLORS
    // ============================================================

    @Size(max = 20, message = "Primary color maksimal 20 karakter")
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$|^rgba?\\([^)]+\\)$|^$",
            message = "Format warna tidak valid (contoh: #3b82f6)"
    )
    private String primaryColor;

    @Size(max = 20)
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$|^rgba?\\([^)]+\\)$|^$",
            message = "Format warna tidak valid"
    )
    private String accentColor;

    @Size(max = 20)
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$|^rgba?\\([^)]+\\)$|^$",
            message = "Format warna tidak valid"
    )
    private String bgColor;

    @Size(max = 20)
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$|^rgba?\\([^)]+\\)$|^$",
            message = "Format warna tidak valid"
    )
    private String textColor;

    // ============================================================
    // TYPOGRAPHY
    // ============================================================

    @Size(max = 100, message = "Font family maksimal 100 karakter")
    private String fontFamily;

    @Size(max = 100)
    private String headingFont;

    // ============================================================
    // LAYOUT
    // ============================================================

    @Size(max = 20)
    private String borderRadius;

    /**
     * GRID | LIST | COMPACT
     */
    private String layout;

    /**
     * Logo config — pipe-separated format.
     */
    @Size(max = 100, message = "Logo icon maksimal 100 karakter")
    @Pattern(
            regexp = "^[A-Za-z0-9 .:/|&+\\-]*$",
            message = "Format logo icon tidak valid"
    )
    private String logoIcon;

    /**
     * LIGHT | DARK
     */
    private String defaultMode;

    /**
     * Nama preset (kalau pakai preset).
     */
    @Size(max = 50)
    private String preset;

    // ============================================================
    // TIMESTAMPS
    // ============================================================

    private LocalDateTime updatedAt;
}