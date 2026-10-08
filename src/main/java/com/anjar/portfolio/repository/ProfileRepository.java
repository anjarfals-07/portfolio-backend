package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // ===== MULTI-TENANT (BARU) =====
    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // ===== PUBLIC (by user slug) (BARU) =====
    Optional<Profile> findByUserPortfolioSlug(String portfolioSlug);

    // ===== LAMA (hapus nanti kalau udah gak dipakai) =====
    default Optional<Profile> findFirst() {
        return findAll().stream().findFirst();
    }
}