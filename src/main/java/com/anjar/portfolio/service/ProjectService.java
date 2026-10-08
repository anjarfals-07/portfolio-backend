package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ProjectDTO;
import com.anjar.portfolio.entity.Project;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ProjectRepository;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ===== GET ALL (by userId) =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ===== GET PUBLISHED (by userId) =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getPublishedProjects(Long userId) {
        return projectRepository.findByUserIdAndPublishedTrueOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ===== GET FEATURED (by userId) =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getFeaturedProjects(Long userId) {
        return projectRepository.findByUserIdAndFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ===== GET BY ID (ownership check) =====
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id, Long userId) {
        Project project = findOwnedProject(id, userId);
        return toDTO(project);
    }

    // ===== GET BY SLUG (public, by username) =====
    @Transactional(readOnly = true)
    public ProjectDTO getPublicProject(String portfolioSlug, String projectSlug) {
        Project project = projectRepository
                .findByUserPortfolioSlugAndSlugAndPublishedTrue(portfolioSlug, projectSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "slug", projectSlug));
        return toDTO(project);
    }

    // ===== GET PUBLIC LIST (by portfolioSlug) — BARU =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getPublicProjects(String portfolioSlug) {
        return projectRepository
                .findByUserPortfolioSlugAndPublishedTrueOrderByCreatedAtDesc(portfolioSlug)
                .stream().map(this::toDTO).toList();
    }

    // ===== CREATE =====
    @Transactional
    public ProjectDTO createProject(Long userId, ProjectDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String slug = (dto.getSlug() == null || dto.getSlug().isBlank())
                ? SlugUtil.toSlug(dto.getTitle())
                : SlugUtil.toSlug(dto.getSlug());

        String uniqueSlug = ensureUniqueSlug(userId, slug, null);

        Project project = Project.builder()
                .user(user)
                .title(dto.getTitle())
                .slug(uniqueSlug)
                .description(dto.getDescription())
                .content(dto.getContent())
                .thumbnailUrl(dto.getThumbnailUrl())
                .techStack(dto.getTechStack())
                .githubUrl(dto.getGithubUrl())
                .demoUrl(dto.getDemoUrl())
                .featured(dto.getFeatured() != null ? dto.getFeatured() : false)
                .published(dto.getPublished() != null ? dto.getPublished() : true)
                .build();

        return toDTO(projectRepository.save(project));
    }

    // ===== UPDATE (ownership check) =====
    @Transactional
    public ProjectDTO updateProject(Long id, Long userId, ProjectDTO dto) {
        Project project = findOwnedProject(id, userId);

        if (dto.getTitle() != null) project.setTitle(dto.getTitle());

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            String slug = SlugUtil.toSlug(dto.getSlug());
            project.setSlug(ensureUniqueSlug(userId, slug, id));
        }

        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getContent() != null) project.setContent(dto.getContent());
        if (dto.getThumbnailUrl() != null) project.setThumbnailUrl(dto.getThumbnailUrl());
        if (dto.getTechStack() != null) project.setTechStack(dto.getTechStack());
        if (dto.getGithubUrl() != null) project.setGithubUrl(dto.getGithubUrl());
        if (dto.getDemoUrl() != null) project.setDemoUrl(dto.getDemoUrl());
        if (dto.getFeatured() != null) project.setFeatured(dto.getFeatured());
        if (dto.getPublished() != null) project.setPublished(dto.getPublished());

        return toDTO(projectRepository.save(project));
    }

    // ===== DELETE (ownership check) =====
    @Transactional
    public void deleteProject(Long id, Long userId) {
        Project project = findOwnedProject(id, userId);
        projectRepository.delete(project);
    }



    // ===== HELPER: Find owned project =====
    private Project findOwnedProject(Long id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        if (!project.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Project ini bukan milik kamu");
        }
        return project;
    }

    // ===== HELPER: Unique slug PER USER =====
    private String ensureUniqueSlug(Long userId, String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (projectRepository.existsByUserIdAndSlug(userId, slug)) {
            if (excludeId != null) {
                Project existing = projectRepository.findByUserIdAndSlug(userId, slug).orElse(null);
                if (existing != null && existing.getId().equals(excludeId)) {
                    return slug;
                }
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
        return slug;
    }

    // ===== Mapper =====
    private ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .description(p.getDescription())
                .content(p.getContent())
                .thumbnailUrl(p.getThumbnailUrl())
                .techStack(p.getTechStack())
                .githubUrl(p.getGithubUrl())
                .demoUrl(p.getDemoUrl())
                .featured(p.getFeatured())
                .published(p.getPublished())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}