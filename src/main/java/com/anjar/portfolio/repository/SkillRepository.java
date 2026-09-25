package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // ===== MULTI-TENANT (BARU) =====
    List<Skill> findByUserIdOrderByCategoryAscSortOrderAsc(Long userId);

    List<Skill> findByUserIdAndCategoryOrderBySortOrderAsc(Long userId, String category);

    long countByUserId(Long userId);

    // ===== PUBLIC (by user slug) (BARU) =====
    List<Skill> findByUserPortfolioSlugOrderByCategoryAscSortOrderAsc(String portfolioSlug);

    // ===== LAMA =====
    List<Skill> findAllByOrderByCategoryAscSortOrderAsc();
    List<Skill> findByCategoryOrderBySortOrderAsc(String category);
}