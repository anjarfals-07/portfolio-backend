package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    // ===== MULTI-TENANT (by userId) =====
    List<BlogPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BlogPost> findByUserIdAndPublishedTrueOrderByCreatedAtDesc(Long userId);

    List<BlogPost> findByUserIdAndFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc(Long userId);

    Optional<BlogPost> findByUserIdAndSlug(Long userId, String slug);

    boolean existsByUserIdAndSlug(Long userId, String slug);

    long countByUserId(Long userId);

    long countByUserIdAndPublishedTrue(Long userId);

    // ===== PUBLIC (by portfolioSlug) =====
    List<BlogPost> findByUserPortfolioSlugAndPublishedTrueOrderByCreatedAtDesc(String portfolioSlug);

    List<BlogPost> findByUserPortfolioSlugAndFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc(String portfolioSlug);

    Optional<BlogPost> findByUserPortfolioSlugAndSlugAndPublishedTrue(String portfolioSlug, String slug);

    // ===== SUPER ADMIN (global) =====
    List<BlogPost> findAllByOrderByCreatedAtDesc();
}