package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.TechStackDTO;
import com.anjar.portfolio.entity.TechStack;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.TechStackRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechStackService {

    private final TechStackRepository techStackRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TechStackDTO> getAll(Long userId) {
        return techStackRepository.findByUserIdOrderBySortOrderAscNameAsc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public TechStackDTO getById(Long id, Long userId) {
        return toDTO(findOwned(id, userId));
    }

    // ===== GET PUBLIC (by portfolioSlug) =====
    @Transactional(readOnly = true)
    public List<TechStackDTO> getPublicList(String portfolioSlug) {
        return techStackRepository
                .findByUserPortfolioSlugOrderBySortOrderAscNameAsc(portfolioSlug)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public TechStackDTO create(Long userId, TechStackDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        TechStack t = TechStack.builder()
                .user(user)
                .name(dto.getName())
                .icon(dto.getIcon())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();
        return toDTO(techStackRepository.save(t));
    }

    @Transactional
    public TechStackDTO update(Long id, Long userId, TechStackDTO dto) {
        TechStack t = findOwned(id, userId);

        if (dto.getName() != null) t.setName(dto.getName());
        if (dto.getIcon() != null) t.setIcon(dto.getIcon());
        if (dto.getSortOrder() != null) t.setSortOrder(dto.getSortOrder());

        return toDTO(techStackRepository.save(t));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        TechStack t = findOwned(id, userId);
        techStackRepository.delete(t);
    }

    private TechStack findOwned(Long id, Long userId) {
        TechStack t = techStackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TechStack", id));
        if (!t.getUser().getId().equals(userId)) {
            throw new ForbiddenException("TechStack ini bukan milik kamu");
        }
        return t;
    }

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