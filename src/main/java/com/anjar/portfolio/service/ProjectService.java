package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ProjectDTO;
import com.anjar.portfolio.entity.Project;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ProjectRepository;
import com.anjar.portfolio.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    // ===== GET ALL =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects(boolean onlyPublished) {
        List<Project> projects = onlyPublished
                ? projectRepository.findByPublishedTrueOrderByCreatedAtDesc()
                : projectRepository.findAll();
        return projects.stream().map(this::toDTO).toList();
    }

    // ===== GET FEATURED =====
    @Transactional(readOnly = true)
    public List<ProjectDTO> getFeaturedProjects() {
        return projectRepository.findByFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        return toDTO(project);
    }

    // ===== GET BY SLUG =====
    @Transactional(readOnly = true)
    public ProjectDTO getProjectBySlug(String slug) {
        Project project = projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "slug", slug));
        return toDTO(project);
    }

    // ===== CREATE =====
    @Transactional
    public ProjectDTO createProject(ProjectDTO dto) {
        String slug = (dto.getSlug() == null || dto.getSlug().isBlank())
                ? SlugUtil.toSlug(dto.getTitle())
                : SlugUtil.toSlug(dto.getSlug());

        // Pastikan slug unik
        String uniqueSlug = ensureUniqueSlug(slug, null);

        Project project = Project.builder()
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

    // ===== UPDATE =====
    @Transactional
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));

        if (dto.getTitle() != null) project.setTitle(dto.getTitle());

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            String slug = SlugUtil.toSlug(dto.getSlug());
            project.setSlug(ensureUniqueSlug(slug, id));
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

    // ===== DELETE =====
    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepository.deleteById(id);
    }

    // ===== HELPER: Mapping Entity -> DTO =====
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

    // ===== HELPER: Pastikan slug unik =====
    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (projectRepository.existsBySlug(slug)) {
            // Kalau update dan slug sama dengan dirinya sendiri, skip
            if (excludeId != null) {
                Project existing = projectRepository.findBySlug(slug).orElse(null);
                if (existing != null && existing.getId().equals(excludeId)) {
                    return slug;
                }
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
        return slug;
    }
}