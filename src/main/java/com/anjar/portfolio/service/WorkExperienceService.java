package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.WorkExperienceDTO;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.entity.WorkExperience;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.repository.WorkExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk manage WorkExperience (riwayat pekerjaan).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkExperienceService {

    private final WorkExperienceRepository workRepository;
    private final UserRepository userRepository;

    // ============================================================
    // LIST (PRIVATE — by userId)
    // ============================================================
    @Transactional(readOnly = true)
    public List<WorkExperienceDTO> listByUser(Long userId) {
        return workRepository
                .findByUserIdOrderBySortOrderAscStartDateDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ⭐ LIST (PUBLIC — by portfolioSlug) — BARU
    // ============================================================
    @Transactional(readOnly = true)
    public List<WorkExperienceDTO> listPublic(String portfolioSlug) {
        return workRepository
                .findByUserPortfolioSlugOrderBySortOrderAscStartDateDesc(portfolioSlug)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @Transactional(readOnly = true)
    public WorkExperienceDTO getOne(Long userId, Long id) {
        WorkExperience work = workRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work experience tidak ditemukan: " + id));
        return toDTO(work);
    }

    // ============================================================
    // CREATE
    // ============================================================
    @Transactional
    public WorkExperienceDTO create(Long userId, WorkExperienceDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Integer sortOrder = dto.getSortOrder();
        if (sortOrder == null) {
            sortOrder = workRepository.findMaxSortOrderByUserId(userId) + 1;
        }

        boolean currentlyHere = Boolean.TRUE.equals(dto.getCurrentlyHere());

        WorkExperience work = WorkExperience.builder()
                .user(user)
                .company(dto.getCompany())
                .position(dto.getPosition())
                .employmentType(dto.getEmploymentType())
                .location(dto.getLocation())
                .startDate(dto.getStartDate())
                .endDate(currentlyHere ? null : dto.getEndDate())
                .currentlyHere(currentlyHere)
                .description(dto.getDescription())
                .sortOrder(sortOrder)
                .build();

        WorkExperience saved = workRepository.save(work);
        log.info("✅ WorkExperience created for user {}: id={}, company={}",
                userId, saved.getId(), saved.getCompany());
        return toDTO(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public WorkExperienceDTO update(Long userId, Long id, WorkExperienceDTO dto) {
        WorkExperience work = workRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work experience tidak ditemukan: " + id));

        if (dto.getCompany() != null) work.setCompany(dto.getCompany());
        if (dto.getPosition() != null) work.setPosition(dto.getPosition());
        if (dto.getEmploymentType() != null) work.setEmploymentType(dto.getEmploymentType());
        if (dto.getLocation() != null) work.setLocation(dto.getLocation());
        if (dto.getStartDate() != null) work.setStartDate(dto.getStartDate());
        if (dto.getDescription() != null) work.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) work.setSortOrder(dto.getSortOrder());

        if (dto.getCurrentlyHere() != null) {
            work.setCurrentlyHere(dto.getCurrentlyHere());
            if (Boolean.TRUE.equals(dto.getCurrentlyHere())) {
                work.setEndDate(null);
            } else if (dto.getEndDate() != null) {
                work.setEndDate(dto.getEndDate());
            }
        } else if (dto.getEndDate() != null) {
            work.setEndDate(dto.getEndDate());
        }

        WorkExperience saved = workRepository.save(work);
        log.info("✏️ WorkExperience updated for user {}: id={}", userId, id);
        return toDTO(saved);
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Transactional
    public void delete(Long userId, Long id) {
        WorkExperience work = workRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Work experience tidak ditemukan: " + id));
        workRepository.delete(work);
        log.info("🗑️ WorkExperience deleted for user {}: id={}", userId, id);
    }

    // ============================================================
    // REORDER
    // ============================================================
    @Transactional
    public void reorder(Long userId, List<Long> orderedIds) {
        int index = 0;
        for (Long id : orderedIds) {
            WorkExperience work = workRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Work experience tidak ditemukan: " + id));
            work.setSortOrder(index++);
            workRepository.save(work);
        }
        log.info("🔀 WorkExperience reordered for user {}: {} items",
                userId, orderedIds.size());
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private WorkExperienceDTO toDTO(WorkExperience w) {
        return WorkExperienceDTO.builder()
                .id(w.getId())
                .company(w.getCompany())
                .position(w.getPosition())
                .employmentType(w.getEmploymentType())
                .location(w.getLocation())
                .startDate(w.getStartDate())
                .endDate(w.getEndDate())
                .currentlyHere(w.getCurrentlyHere())
                .description(w.getDescription())
                .sortOrder(w.getSortOrder())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }
}