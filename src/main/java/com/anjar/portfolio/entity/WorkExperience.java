package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity WorkExperience — riwayat pekerjaan user.
 *
 * Relasi: Many-to-One ke User.
 * Setiap user bisa punya banyak entry pekerjaan.
 */
@Entity
@Table(name = "work_experiences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperience {

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
    // DATA PEKERJAAN
    // ============================================================
    @Column(nullable = false, length = 255)
    private String company;

    @Column(nullable = false, length = 255)
    private String position;

    /**
     * FULL_TIME | PART_TIME | CONTRACT | FREELANCE | INTERNSHIP
     */
    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(length = 200)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "currently_here", nullable = false)
    @Builder.Default
    private Boolean currentlyHere = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ============================================================
    // ORDERING
    // ============================================================
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // ============================================================
    // TIMESTAMPS
    // ============================================================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (currentlyHere == null) currentlyHere = false;
        if (sortOrder == null) sortOrder = 0;
    }
}