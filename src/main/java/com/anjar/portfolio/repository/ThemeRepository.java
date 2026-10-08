package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    // ============================================================
    // BY USER ID
    // ============================================================

    /**
     * Cari theme by user ID.
     *
     * Karena relasi OneToOne, cuma ada 1 theme per user.
     */
    Optional<Theme> findByUserId(Long userId);

    /**
     * Cek apakah user udah punya theme.
     */
    boolean existsByUserId(Long userId);

    // ============================================================
    // BY PORTFOLIO SLUG (PUBLIC)
    // ============================================================

    /**
     * Cari theme by user's portfolio slug.
     * Dipakai di public endpoint: /api/users/{username}/theme
     */
    Optional<Theme> findByUserPortfolioSlug(String portfolioSlug);

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * Hapus theme by user ID.
     */
    void deleteByUserId(Long userId);
}