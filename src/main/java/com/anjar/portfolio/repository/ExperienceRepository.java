package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    // Sort by sort_order ASC, lalu created_at DESC
    List<Experience> findAllByOrderBySortOrderAscCreatedAtDesc();
}