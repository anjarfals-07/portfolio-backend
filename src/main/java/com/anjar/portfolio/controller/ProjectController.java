package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ProjectDTO;
import com.anjar.portfolio.service.ProjectService;
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

    // ===== GET ALL (public) =====
    // GET /api/projects?all=true
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects(
            @RequestParam(defaultValue = "false") boolean all) {
        // Kalau `all=true`, tampilkan semua (termasuk draft)
        // Default: cuma yang published
        return ResponseEntity.ok(projectService.getAllProjects(!all));
    }

    // ===== GET FEATURED =====
    // GET /api/projects/featured
    @GetMapping("/featured")
    public ResponseEntity<List<ProjectDTO>> getFeaturedProjects() {
        return ResponseEntity.ok(projectService.getFeaturedProjects());
    }

    // ===== GET BY ID =====
    // GET /api/projects/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // ===== GET BY SLUG =====
    // GET /api/projects/slug/{slug}
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProjectDTO> getProjectBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(projectService.getProjectBySlug(slug));
    }

    // ===== CREATE =====
    // POST /api/projects
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        ProjectDTO created = projectService.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ===== UPDATE =====
    // PUT /api/projects/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        return ResponseEntity.ok(projectService.updateProject(id, dto));
    }

    // ===== DELETE =====
    // DELETE /api/projects/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}