package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // ===== MULTI-TENANT (BARU) =====
    List<Experience> findByUserIdOrderBySortOrderAscCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    // ===== PUBLIC (by user slug) (BARU) =====
    List<Experience> findByUserPortfolioSlugOrderBySortOrderAscCreatedAtDesc(String portfolioSlug);

    // ===== LAMA =====
    List<Experience> findAllByOrderBySortOrderAscCreatedAtDesc();
}