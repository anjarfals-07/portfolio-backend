package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.TechStackDTO;
import com.anjar.portfolio.entity.TechStack;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechStackService {

    private final TechStackRepository techStackRepository;

    // ===== GET ALL =====
    @Transactional(readOnly = true)
    public List<TechStackDTO> getAll() {
        return techStackRepository.findAllByOrderBySortOrderAscNameAsc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public TechStackDTO getById(Long id) {
        TechStack t = techStackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TechStack", id));
        return toDTO(t);
    }

    // ===== CREATE =====
    @Transactional
    public TechStackDTO create(TechStackDTO dto) {
        TechStack t = TechStack.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDTO(techStackRepository.save(t));
    }

    // ===== UPDATE =====
    @Transactional
    public TechStackDTO update(Long id, TechStackDTO dto) {
        TechStack t = techStackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TechStack", id));

        if (dto.getName() != null) t.setName(dto.getName());
        if (dto.getIcon() != null) t.setIcon(dto.getIcon());
        if (dto.getSortOrder() != null) t.setSortOrder(dto.getSortOrder());

        return toDTO(techStackRepository.save(t));
    }

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        if (!techStackRepository.existsById(id)) {
            throw new ResourceNotFoundException("TechStack", id);
        }
        techStackRepository.deleteById(id);
    }

    // ===== Mapper =====
    private TechStackDTO toDTO(TechStack t) {
        return TechStackDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .icon(t.getIcon())
                .sortOrder(t.getSortOrder())
                .createdAt(t.getCreatedAt())
                .build();
    }
}