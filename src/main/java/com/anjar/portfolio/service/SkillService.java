package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.SkillDTO;
import com.anjar.portfolio.entity.Skill;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    // ===== GET ALL (flat list) =====
    @Transactional(readOnly = true)
    public List<SkillDTO> getAll() {
        return skillRepository.findAllByOrderByCategoryAscSortOrderAsc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET GROUPED BY CATEGORY =====
    // Output: { "Backend": { icon, items: [...] }, "Frontend": {...} }
    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> getGrouped() {
        List<Skill> all = skillRepository.findAllByOrderByCategoryAscSortOrderAsc();
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();

        for (Skill s : all) {
            String category = s.getCategory() != null ? s.getCategory() : "Other";
            grouped.computeIfAbsent(category, k -> {
                Map<String, Object> cat = new LinkedHashMap<>();
                cat.put("category", category);
                cat.put("categoryIcon", s.getCategoryIcon());
                cat.put("items", new java.util.ArrayList<SkillDTO>());
                return cat;
            });
            @SuppressWarnings("unchecked")
            List<SkillDTO> items = (List<SkillDTO>) grouped.get(category).get("items");
            items.add(toDTO(s));
        }
        return grouped;
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public SkillDTO getById(Long id) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
        return toDTO(s);
    }

    // ===== CREATE =====
    @Transactional
    public SkillDTO create(SkillDTO dto) {
        Skill s = Skill.builder()
                .category(dto.getCategory())
                .categoryIcon(dto.getCategoryIcon())
                .name(dto.getName())
                .level(dto.getLevel())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDTO(skillRepository.save(s));
    }

    // ===== UPDATE =====
    @Transactional
    public SkillDTO update(Long id, SkillDTO dto) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));

        if (dto.getCategory() != null) s.setCategory(dto.getCategory());
        if (dto.getCategoryIcon() != null) s.setCategoryIcon(dto.getCategoryIcon());
        if (dto.getName() != null) s.setName(dto.getName());
        if (dto.getLevel() != null) s.setLevel(dto.getLevel());
        if (dto.getSortOrder() != null) s.setSortOrder(dto.getSortOrder());

        return toDTO(skillRepository.save(s));
    }

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill", id);
        }
        skillRepository.deleteById(id);
    }

    // ===== Mapper =====
    private SkillDTO toDTO(Skill s) {
        return SkillDTO.builder()
                .id(s.getId())
                .category(s.getCategory())
                .categoryIcon(s.getCategoryIcon())
                .name(s.getName())
                .level(s.getLevel())
                .sortOrder(s.getSortOrder())
                .createdAt(s.getCreatedAt())
                .build();
    }
}