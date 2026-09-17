package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Cari project by slug (untuk halaman detail)
    Optional<Project> findBySlug(String slug);

    // Cek slug udah ada belum (buat validasi)
    boolean existsBySlug(String slug);

    // Ambil project yang published aja, urut terbaru
    List<Project> findByPublishedTrueOrderByCreatedAtDesc();

    // Ambil project featured (buat halaman home)
    List<Project> findByFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc();
}