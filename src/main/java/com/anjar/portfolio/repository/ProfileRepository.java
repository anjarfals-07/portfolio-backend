package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // Ambil profil pertama (karena cuma 1 row)
    default Optional<Profile> findFirst() {
        return findAll().stream().findFirst();
    }
}