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
 * DTO Education — request/response untuk riwayat pendidikan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {

    private Long id;

    // ============================================================
    // DATA PENDIDIKAN
    // ============================================================
    @NotBlank(message = "Nama institusi wajib diisi")
    @Size(max = 255, message = "Institusi maksimal 255 karakter")
    private String institution;

    @Size(max = 100, message = "Jenjang maksimal 100 karakter")
    private String degree;

    @Size(max = 255, message = "Jurusan maksimal 255 karakter")
    private String fieldOfStudy;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 20, message = "GPA maksimal 20 karakter")
    private String gpa;

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