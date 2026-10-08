package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ProjectDTO;
import com.anjar.portfolio.service.ProjectService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ProjectController {

    private final ProjectService projectService;

    // ===== GET ALL (OWNER) =====
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getAllProjects(userId));
    }

    // ===== GET PUBLISHED (OWNER) =====
    @GetMapping("/published")
    public ResponseEntity<List<ProjectDTO>> getPublishedProjects() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getPublishedProjects(userId));
    }

    // ===== GET FEATURED (OWNER) =====
    @GetMapping("/featured")
    public ResponseEntity<List<ProjectDTO>> getFeaturedProjects() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getFeaturedProjects(userId));
    }

    // ===== GET BY ID (OWNER) =====
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    // ===== PUBLIC — GET BY USER SLUG =====
    @GetMapping("/public/{portfolioSlug}")
    public ResponseEntity<List<ProjectDTO>> getPublicProjects(@PathVariable String portfolioSlug) {
        return ResponseEntity.ok(projectService.getPublicProjects(portfolioSlug));
    }

    // ===== PUBLIC — GET DETAIL =====
    @GetMapping("/public/{portfolioSlug}/{projectSlug}")
    public ResponseEntity<ProjectDTO> getPublicProject(
            @PathVariable String portfolioSlug,
            @PathVariable String projectSlug) {
        return ResponseEntity.ok(projectService.getPublicProject(portfolioSlug, projectSlug));
    }

    // ===== CREATE (OWNER) =====
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(userId, dto));
    }

    // ===== UPDATE (OWNER) =====
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.updateProject(id, userId, dto));
    }

    // ===== DELETE (OWNER) =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }
}