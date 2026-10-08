package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO Profile — request/response untuk data portfolio user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private Long id;

    // ============================================================
    // IDENTITAS
    // ============================================================
    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 200, message = "Nama maksimal 200 karakter")
    private String fullName;

    @Size(max = 200, message = "Role maksimal 200 karakter")
    private String role;

    private String bio;

    @Size(max = 500, message = "Short bio maksimal 500 karakter")
    private String shortBio;

    // ============================================================
    // KONTAK
    // ============================================================
    @Email(message = "Format email tidak valid")
    @Size(max = 200, message = "Email maksimal 200 karakter")
    private String email;

    @Size(max = 50, message = "No. telepon maksimal 50 karakter")
    private String phone;

    @Size(max = 200, message = "Kota maksimal 200 karakter")
    private String city;

    @Size(max = 500, message = "Alamat maksimal 500 karakter")
    private String location;

    // ============================================================
    // ⭐ PERSONAL INFO (BARU)
    // ============================================================
    @Size(max = 50, message = "Agama maksimal 50 karakter")
    private String religion;

    /**
     * Format: SINGLE | MARRIED | DIVORCED | WIDOWED
     */
    @Size(max = 30, message = "Status pernikahan maksimal 30 karakter")
    private String maritalStatus;

    private LocalDate birthDate;

    @Size(max = 200, message = "Tempat lahir maksimal 200 karakter")
    private String birthPlace;

    @Size(max = 100, message = "Kewarganegaraan maksimal 100 karakter")
    private String nationality;

    /**
     * Format: MALE | FEMALE | OTHER
     */
    @Size(max = 20, message = "Gender maksimal 20 karakter")
    private String gender;

    // ============================================================
    // MEDIA
    // ============================================================
    @Size(max = 500, message = "URL avatar maksimal 500 karakter")
    private String avatarUrl;

    @Size(max = 500, message = "URL CV maksimal 500 karakter")
    private String cvUrl;

    // ============================================================
    // CV METADATA
    // ============================================================
    /**
     * Source CV: "UPLOAD" / "GENERATED" / null.
     */
    private String cvSource;

    /**
     * Timestamp kapan CV di-generate.
     */
    private LocalDateTime cvGeneratedAt;

    // ============================================================
    // CV PREFERENCES (JSONB)
    // ============================================================
    /**
     * Preferensi customize CV user.
     *
     * Struktur (semua optional):
     * {
     *   "template": "modern",
     *   "layout": "sidebar-left",
     *   "theme": "light",
     *   "density": "normal",
     *   "palette": "ocean",
     *   "accentColor": "#3b82f6",
     *   "fontPair": "inter-inter",
     *   "iconSet": "primeicons",
     *   "cardStyle": "soft",
     *   "badgeStyle": "pill",
     *   "backgroundPattern": "none",
     *   "showBio": true,
     *   "showPersonalInfo": true,
     *   "showEducation": true,
     *   "showWorkExperience": true,
     *   "showExperiences": true,
     *   "showProjects": true,
     *   "showSkills": true,
     *   "showTechStack": true
     * }
     */
    private Map<String, Object> cvPreferences;

    // ============================================================
    // STATUS
    // ============================================================
    private Boolean availableForWork;

    // ============================================================
    // SOCIALS
    // ============================================================
    /**
     * List sosial media.
     * Format: [{"icon": "pi pi-github", "url": "...", "label": "GitHub"}]
     */
    private List<Map<String, String>> socials;

    // ============================================================
    // TIMESTAMPS
    // ============================================================
    private LocalDateTime updatedAt;
}