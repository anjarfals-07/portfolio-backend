package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.EducationDTO;
import com.anjar.portfolio.entity.Education;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.EducationRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service untuk manage Education (riwayat pendidikan).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    // ============================================================
    // LIST (PRIVATE — by userId)
    // ============================================================
    @Transactional(readOnly = true)
    public List<EducationDTO> listByUser(Long userId) {
        return educationRepository
                .findByUserIdOrderBySortOrderAscStartDateDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ⭐ LIST (PUBLIC — by portfolioSlug) — BARU
    // ============================================================
    @Transactional(readOnly = true)
    public List<EducationDTO> listPublic(String portfolioSlug) {
        return educationRepository
                .findByUserPortfolioSlugOrderBySortOrderAscStartDateDesc(portfolioSlug)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @Transactional(readOnly = true)
    public EducationDTO getOne(Long userId, Long id) {
        Education edu = educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Education tidak ditemukan: " + id));
        return toDTO(edu);
    }

    // ============================================================
    // CREATE
    // ============================================================
    @Transactional
    public EducationDTO create(Long userId, EducationDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Integer sortOrder = dto.getSortOrder();
        if (sortOrder == null) {
            sortOrder = educationRepository.findMaxSortOrderByUserId(userId) + 1;
        }

        Education edu = Education.builder()
                .user(user)
                .institution(dto.getInstitution())
                .degree(dto.getDegree())
                .fieldOfStudy(dto.getFieldOfStudy())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .gpa(dto.getGpa())
                .description(dto.getDescription())
                .sortOrder(sortOrder)
                .build();

        Education saved = educationRepository.save(edu);
        log.info("✅ Education created for user {}: id={}, institution={}",
                userId, saved.getId(), saved.getInstitution());
        return toDTO(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Transactional
    public EducationDTO update(Long userId, Long id, EducationDTO dto) {
        Education edu = educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Education tidak ditemukan: " + id));

        if (dto.getInstitution() != null) edu.setInstitution(dto.getInstitution());
        if (dto.getDegree() != null) edu.setDegree(dto.getDegree());
        if (dto.getFieldOfStudy() != null) edu.setFieldOfStudy(dto.getFieldOfStudy());
        if (dto.getStartDate() != null) edu.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) edu.setEndDate(dto.getEndDate());
        if (dto.getGpa() != null) edu.setGpa(dto.getGpa());
        if (dto.getDescription() != null) edu.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) edu.setSortOrder(dto.getSortOrder());

        Education saved = educationRepository.save(edu);
        log.info("✏️ Education updated for user {}: id={}", userId, id);
        return toDTO(saved);
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Transactional
    public void delete(Long userId, Long id) {
        Education edu = educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Education tidak ditemukan: " + id));
        educationRepository.delete(edu);
        log.info("🗑️ Education deleted for user {}: id={}", userId, id);
    }

    // ============================================================
    // REORDER
    // ============================================================
    @Transactional
    public void reorder(Long userId, List<Long> orderedIds) {
        int index = 0;
        for (Long id : orderedIds) {
            Education edu = educationRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Education tidak ditemukan: " + id));
            edu.setSortOrder(index++);
            educationRepository.save(edu);
        }
        log.info("🔀 Education reordered for user {}: {} items",
                userId, orderedIds.size());
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private EducationDTO toDTO(Education e) {
        return EducationDTO.builder()
                .id(e.getId())
                .institution(e.getInstitution())
                .degree(e.getDegree())
                .fieldOfStudy(e.getFieldOfStudy())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .gpa(e.getGpa())
                .description(e.getDescription())
                .sortOrder(e.getSortOrder())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}