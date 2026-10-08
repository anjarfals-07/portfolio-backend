package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserRole;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.enums.UserPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============================================================
    // AUTH
    // ============================================================
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    // ============================================================
    // EMAIL
    // ============================================================
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);   // ⭐ TAMBAH INI

    // ============================================================
    // RESET TOKEN
    // ============================================================
    Optional<User> findByResetToken(String resetToken);

    // ============================================================
    // MULTI-TENANT
    // ============================================================
    Optional<User> findByPortfolioSlug(String portfolioSlug);
    boolean existsByPortfolioSlug(String portfolioSlug);

    // ============================================================
    // ROLE-BASED
    // ============================================================
    List<User> findAllByRole(UserRole role);
    List<User> findAllByActiveTrueOrderByCreatedAtDesc();
    long countByRole(UserRole role);
    long countByActiveTrue();

    // ============================================================
    // STATUS-BASED ⭐ NEW (Phase 3)
    // ============================================================
    List<User> findAllByStatusOrderByCreatedAtDesc(UserStatus status);
    long countByStatus(UserStatus status);

    /**
     * Cari user PENDING_PAYMENT yang sudah lama (untuk cleanup).
     * User yang register > X jam lalu tapi belum bayar.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.status = com.anjar.portfolio.enums.UserStatus.PENDING_PAYMENT
          AND u.createdAt < :cutoff
    """)
    List<User> findStalePendingPayment(@Param("cutoff") LocalDateTime cutoff);

    // ============================================================
    // STATS
    // ============================================================
    @Query("""
        SELECT u FROM User u
        WHERE u.active = true
        ORDER BY u.createdAt DESC
    """)
    List<User> findAllActiveForDirectory();

    /**
     * Count user per status — untuk admin dashboard.
     */
    @Query("""
        SELECT u.status, COUNT(u)
        FROM User u
        GROUP BY u.status
    """)
    List<Object[]> countGroupByStatus();

    // ============================================================
    // SEARCH (admin)
    // ============================================================
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%'))
        ORDER BY u.createdAt DESC
    """)
    List<User> searchByKeyword(@Param("q") String q);

    List<User> findByStatusAndPaymentStatusAndUpdatedAtBefore(
            UserStatus status,
            UserPaymentStatus paymentStatus,
            LocalDateTime cutoff
    );
}