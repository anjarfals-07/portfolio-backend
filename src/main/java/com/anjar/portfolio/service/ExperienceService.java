package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ExperienceDTO;
import com.anjar.portfolio.entity.Experience;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    // ===== GET ALL =====
    @Transactional(readOnly = true)
    public List<ExperienceDTO> getAll() {
        return experienceRepository.findAllByOrderBySortOrderAscCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public ExperienceDTO getById(Long id) {
        Experience e = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", id));
        return toDTO(e);
    }

    // ===== CREATE =====
    @Transactional
    public ExperienceDTO create(ExperienceDTO dto) {
        Experience e = Experience.builder()
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

    // ===== UPDATE =====
    @Transactional
    public ExperienceDTO update(Long id, ExperienceDTO dto) {
        Experience e = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", id));

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

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        if (!experienceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Experience", id);
        }
        experienceRepository.deleteById(id);
    }

    // ===== Mapper =====
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