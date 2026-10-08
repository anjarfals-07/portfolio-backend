package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    /**
     * List education user, urut by sortOrder asc, lalu startDate desc.
     */
    List<Education> findByUserIdOrderBySortOrderAscStartDateDesc(Long userId);

    /**
     * Find by id + user_id (buat validasi ownership).
     */
    Optional<Education> findByIdAndUserId(Long id, Long userId);

    /**
     * Count berapa entry education user.
     */
    long countByUserId(Long userId);

    /**
     * Get max sortOrder user — buat append di akhir.
     */
    @Query("SELECT COALESCE(MAX(e.sortOrder), -1) FROM Education e WHERE e.user.id = :userId")
    int findMaxSortOrderByUserId(@Param("userId") Long userId);

    /**
     * Hapus semua education user (misal saat reset profile).
     */
    void deleteByUserId(Long userId);

    // ============================================================
    // ⭐ PUBLIC — by portfolioSlug (untuk halaman publik)
    // ============================================================
    List<Education> findByUserPortfolioSlugOrderBySortOrderAscStartDateDesc(String portfolioSlug);
}