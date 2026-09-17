package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Sort by category, lalu sort_order
    List<Skill> findAllByOrderByCategoryAscSortOrderAsc();

    // Ambil skill per kategori
    List<Skill> findByCategoryOrderBySortOrderAsc(String category);
}