package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO WorkExperience — request/response untuk riwayat pekerjaan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceDTO {

    private Long id;

    // ============================================================
    // DATA PEKERJAAN
    // ============================================================
    @NotBlank(message = "Nama perusahaan wajib diisi")
    @Size(max = 255, message = "Perusahaan maksimal 255 karakter")
    private String company;

    @NotBlank(message = "Posisi wajib diisi")
    @Size(max = 255, message = "Posisi maksimal 255 karakter")
    private String position;

    /**
     * FULL_TIME | PART_TIME | CONTRACT | FREELANCE | INTERNSHIP
     */
    @Size(max = 50, message = "Tipe kerja maksimal 50 karakter")
    private String employmentType;

    @Size(max = 200, message = "Lokasi maksimal 200 karakter")
    private String location;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean currentlyHere;

    private String description;

    // ============================================================
    // ORDERING
    // ============================================================
    private Integer sortOrder;

    // ============================================================
    // TIMESTAMPS (read-only)
    // ============================================================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}