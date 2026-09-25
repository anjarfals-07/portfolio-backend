package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ===== AUTH =====

    /**
     * Cari user by username (untuk login)
     */
    Optional<User> findByUsername(String username);

    /**
     * Cek username exists
     */
    boolean existsByUsername(String username);

    // ===== MULTI-TENANT: Portfolio Slug =====

    /**
     * Cari user by portfolio slug (untuk URL /anjar)
     */
    Optional<User> findByPortfolioSlug(String portfolioSlug);

    /**
     * Cek slug exists
     */
    boolean existsByPortfolioSlug(String portfolioSlug);

    // ===== ROLE-BASED =====

    /**
     * List semua user dengan role tertentu
     */
    List<User> findAllByRole(UserRole role);

    /**
     * List semua user aktif (untuk landing page)
     */
    List<User> findAllByActiveTrueOrderByCreatedAtDesc();

    /**
     * Count user by role
     */
    long countByRole(UserRole role);

    /**
     * Count user aktif
     */
    long countByActiveTrue();

    // ===== STATS =====

    /**
     * List user aktif dengan field minimal (untuk landing page directory)
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.active = true
        ORDER BY u.createdAt DESC
    """)
    List<User> findAllActiveForDirectory();
}