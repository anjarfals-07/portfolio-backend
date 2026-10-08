package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.TenantDomain;
import com.anjar.portfolio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantDomainRepository extends JpaRepository<TenantDomain, Long> {

    // ============================================================
    // LOOKUP BY DOMAIN
    // ============================================================

    /**
     * Cari domain (case-insensitive).
     */
    @Query("SELECT td FROM TenantDomain td WHERE LOWER(td.domain) = LOWER(:domain)")
    Optional<TenantDomain> findByDomainIgnoreCase(@Param("domain") String domain);

    /**
     * Cari domain yang sudah verified (untuk public endpoint).
     */
    @Query("SELECT td FROM TenantDomain td " +
            "WHERE LOWER(td.domain) = LOWER(:domain) " +
            "AND td.isVerified = true")
    Optional<TenantDomain> findVerifiedByDomain(@Param("domain") String domain);

    // ============================================================
    // LIST BY USER
    // ============================================================

    /**
     * List semua domain milik user (sorted by createdAt ASC).
     */
    List<TenantDomain> findByUserOrderByCreatedAtAsc(User user);

    /**
     * List semua domain milik user by userId.
     */
    List<TenantDomain> findByUserIdOrderByCreatedAtAsc(Long userId);

    /**
     * Cari primary domain user.
     */
    @Query("SELECT td FROM TenantDomain td " +
            "WHERE td.user.id = :userId AND td.isPrimary = true")
    Optional<TenantDomain> findPrimaryByUserId(@Param("userId") Long userId);

    // ============================================================
    // EXISTS CHECKS
    // ============================================================

    /**
     * Cek apakah domain sudah dipakai user lain.
     */
    boolean existsByDomainIgnoreCase(String domain);

    /**
     * Cek apakah domain sudah dipakai user lain (exclude id).
     * Untuk validasi saat update.
     */
    @Query("SELECT COUNT(td) > 0 FROM TenantDomain td " +
            "WHERE LOWER(td.domain) = LOWER(:domain) AND td.id <> :excludeId")
    boolean existsByDomainIgnoreCaseAndIdNot(
            @Param("domain") String domain,
            @Param("excludeId") Long excludeId
    );

    /**
     * Hitung jumlah domain milik user.
     */
    long countByUserId(Long userId);
}