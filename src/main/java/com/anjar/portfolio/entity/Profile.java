package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity Profile — data portfolio user.
 *
 * Setiap user punya 1 profile yang berisi:
 * - Identitas (nama, role, bio)
 * - Kontak (email, phone, city, location)
 * - Media (avatar, CV)
 * - Personal info (agama, marital, lahir, dll) ⭐ BARU
 * - Sosial media (JSONB array)
 * - Preferensi CV (JSONB object)
 */
@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // OWNER (Multi-Tenant)
    // ============================================================
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ============================================================
    // IDENTITAS
    // ============================================================
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    /**
     * Role/posisi user untuk DISPLAY di portfolio.
     * Contoh: "Full-Stack Developer", "UI Designer"
     *
     * ⚠️ BUKAN role authorization (OWNER/SUPER_ADMIN) — itu ada di User.role
     */
    @Column(length = 200)
    private String role;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "short_bio", length = 500)
    private String shortBio;

    // ============================================================
    // KONTAK
    // ============================================================
    @Column(length = 200)
    private String email;

    @Column(length = 50)
    private String phone;

    /**
     * Kota/Negara untuk DISPLAY portfolio.
     * Contoh: "Jakarta, Indonesia"
     */
    @Column(length = 200)
    private String city;

    /**
     * Alamat lengkap untuk CV (opsional).
     * Contoh: "Jl. Sudirman No. 123, Jakarta Selatan"
     */
    @Column(length = 500)
    private String location;

    // ============================================================
    // ⭐ PERSONAL INFO (BARU)
    // ============================================================
    /**
     * Agama — Islam, Kristen, Hindu, Budha, Konghucu, dll.
     */
    @Column(length = 50)
    private String religion;

    /**
     * Status pernikahan.
     * Format: SINGLE | MARRIED | DIVORCED | WIDOWED
     */
    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(length = 100)
    private String nationality;

    /**
     * Gender: MALE | FEMALE | OTHER
     */
    @Column(length = 20)
    private String gender;

    // ============================================================
    // MEDIA
    // ============================================================
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "cv_url", length = 500)
    private String cvUrl;

    // ============================================================
    // CV METADATA
    // ============================================================
    @Enumerated(EnumType.STRING)
    @Column(name = "cv_source", length = 20)
    private CvSource cvSource;

    @Column(name = "cv_generated_at")
    private LocalDateTime cvGeneratedAt;

    // ============================================================
    // CV PREFERENCES (JSONB)
    // ============================================================
    /**
     * Preferensi customize CV user.
     *
     * Struktur:
     * {
     *   "template": "modern",
     *   "layout": "sidebar-left",
     *   "theme": "light",
     *   "palette": "ocean",
     *   "fontPair": "inter-inter",
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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cv_preferences", columnDefinition = "jsonb")
    private Map<String, Object> cvPreferences;

    // ============================================================
    // STATUS
    // ============================================================
    @Column(name = "available_for_work")
    @Builder.Default
    private Boolean availableForWork = true;

    // ============================================================
    // SOCIALS (JSONB array)
    // ============================================================
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, String>> socials;

    // ============================================================
    // TIMESTAMPS
    // ============================================================
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // ENUM: CV SOURCE
    // ============================================================
    public enum CvSource {
        UPLOAD,
        GENERATED
    }
}