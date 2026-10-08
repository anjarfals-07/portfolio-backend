package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.SkillDTO;
import com.anjar.portfolio.entity.Skill;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.SkillRepository;
import com.anjar.portfolio.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SkillDTO> getAll(Long userId) {
        return skillRepository.findByUserIdOrderByCategoryAscSortOrderAsc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> getGrouped(Long userId) {
        List<Skill> all = skillRepository.findByUserIdOrderByCategoryAscSortOrderAsc(userId);
        return groupSkills(all);
    }

    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> getPublicGrouped(String portfolioSlug) {
        List<Skill> all = skillRepository
                .findByUserPortfolioSlugOrderByCategoryAscSortOrderAsc(portfolioSlug);
        return groupSkills(all);
    }

    private Map<String, Map<String, Object>> groupSkills(List<Skill> all) {
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

    @Transactional(readOnly = true)
    public SkillDTO getById(Long id, Long userId) {
        return toDTO(findOwned(id, userId));
    }

    @Transactional
    public SkillDTO create(Long userId, SkillDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Skill s = Skill.builder()
                .user(user)
                .category(dto.getCategory())
                .categoryIcon(dto.getCategoryIcon())
                .name(dto.getName())
                .level(dto.getLevel())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDTO(skillRepository.save(s));
    }

    @Transactional
    public SkillDTO update(Long id, Long userId, SkillDTO dto) {
        Skill s = findOwned(id, userId);

        if (dto.getCategory() != null) s.setCategory(dto.getCategory());
        if (dto.getCategoryIcon() != null) s.setCategoryIcon(dto.getCategoryIcon());
        if (dto.getName() != null) s.setName(dto.getName());
        if (dto.getLevel() != null) s.setLevel(dto.getLevel());
        if (dto.getSortOrder() != null) s.setSortOrder(dto.getSortOrder());

        return toDTO(skillRepository.save(s));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Skill s = findOwned(id, userId);
        skillRepository.delete(s);
    }

    private Skill findOwned(Long id, Long userId) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
        if (!s.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Skill ini bukan milik kamu");
        }
        return s;
    }

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