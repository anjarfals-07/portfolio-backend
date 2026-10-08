package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ExperienceDTO;
import com.anjar.portfolio.entity.Experience;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ExperienceRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ExperienceDTO> getAll(Long userId) {
        return experienceRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ExperienceDTO getById(Long id, Long userId) {
        Experience e = findOwned(id, userId);
        return toDTO(e);
    }

    // ===== GET PUBLIC (by portfolioSlug) =====
    @Transactional(readOnly = true)
    public List<ExperienceDTO> getPublicList(String portfolioSlug) {
        return experienceRepository
                .findByUserPortfolioSlugOrderBySortOrderAscCreatedAtDesc(portfolioSlug)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ExperienceDTO create(Long userId, ExperienceDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Experience e = Experience.builder()
                .user(user)
                .year(dto.getYear())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .color(dto.getColor())
                .tags(dto.getTags())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDTO(experienceRepository.save(e));
    }

    @Transactional
    public ExperienceDTO update(Long id, Long userId, ExperienceDTO dto) {
        Experience e = findOwned(id, userId);

        if (dto.getYear() != null) e.setYear(dto.getYear());
        if (dto.getTitle() != null) e.setTitle(dto.getTitle());
        if (dto.getSubtitle() != null) e.setSubtitle(dto.getSubtitle());
        if (dto.getDescription() != null) e.setDescription(dto.getDescription());
        if (dto.getIcon() != null) e.setIcon(dto.getIcon());
        if (dto.getColor() != null) e.setColor(dto.getColor());
        if (dto.getTags() != null) e.setTags(dto.getTags());
        if (dto.getSortOrder() != null) e.setSortOrder(dto.getSortOrder());

        return toDTO(experienceRepository.save(e));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Experience e = findOwned(id, userId);
        experienceRepository.delete(e);
    }

    private Experience findOwned(Long id, Long userId) {
        Experience e = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", id));
        if (!e.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Experience ini bukan milik kamu");
        }
        return e;
    }

    private ExperienceDTO toDTO(Experience e) {
        return ExperienceDTO.builder()
                .id(e.getId())
                .year(e.getYear())
                .title(e.getTitle())
                .subtitle(e.getSubtitle())
                .description(e.getDescription())
                .icon(e.getIcon())
                .color(e.getColor())
                .tags(e.getTags())
                .sortOrder(e.getSortOrder())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}