package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkExperienceRepository extends JpaRepository<WorkExperience, Long> {

    /**
     * List work experience user, urut by sortOrder asc, lalu startDate desc.
     */
    List<WorkExperience> findByUserIdOrderBySortOrderAscStartDateDesc(Long userId);

    /**
     * Find by id + user_id (buat validasi ownership).
     */
    Optional<WorkExperience> findByIdAndUserId(Long id, Long userId);

    /**
     * Count berapa entry work experience user.
     */
    long countByUserId(Long userId);

    /**
     * Get max sortOrder user — buat append di akhir.
     */
    @Query("SELECT COALESCE(MAX(w.sortOrder), -1) FROM WorkExperience w WHERE w.user.id = :userId")
    int findMaxSortOrderByUserId(@Param("userId") Long userId);

    /**
     * Cek ada berapa yang "currently here" (harusnya cuma 1).
     */
    long countByUserIdAndCurrentlyHereTrue(Long userId);

    /**
     * Hapus semua work experience user.
     */
    void deleteByUserId(Long userId);

    // ============================================================
    // ⭐ PUBLIC — by portfolioSlug (untuk halaman publik)
    // ============================================================
    List<WorkExperience> findByUserPortfolioSlugOrderBySortOrderAscStartDateDesc(String portfolioSlug);
}