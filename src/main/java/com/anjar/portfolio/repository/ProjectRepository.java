package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // ===== MULTI-TENANT (BARU) =====
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Project> findByUserIdAndSlug(Long userId, String slug);

    List<Project> findByUserIdAndPublishedTrueOrderByCreatedAtDesc(Long userId);

    List<Project> findByUserIdAndFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndSlug(Long userId, String slug);

    long countByUserId(Long userId);

    // ===== PUBLIC (by user slug) (BARU) =====
    List<Project> findByUserPortfolioSlugAndPublishedTrueOrderByCreatedAtDesc(String portfolioSlug);

    Optional<Project> findByUserPortfolioSlugAndSlugAndPublishedTrue(String portfolioSlug, String slug);

    // ===== LAMA (tetap dipakai untuk admin?) =====
    Optional<Project> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Project> findByPublishedTrueOrderByCreatedAtDesc();
    List<Project> findByFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc();
}