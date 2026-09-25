package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public API — akses portfolio user tanpa login.
 * Base path: /api/users/{username}
 *
 * {username} = portfolioSlug user (misal: anjar, budi, superadmin)
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UserPublicController {

    private final UserService userService;
    private final ProfileService profileService;
    private final ProjectService projectService;
    private final BlogService blogService;
    private final SkillService skillService;
    private final ExperienceService experienceService;
    private final TechStackService techStackService;
    private final MessageService messageService;

    // ============================================================
    // LIST ALL USERS (LANDING PAGE)
    // ============================================================

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserPublicDTO>> listAllUsers() {
        return ResponseEntity.ok(userService.getAllPublicUsers());
    }

    // ============================================================
    // PROFILE
    // ============================================================

    // GET /api/users/{username}
    @GetMapping("/{username}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getPublicProfile(username));
    }

    // ============================================================
    // PROJECTS
    // ============================================================

    // GET /api/users/{username}/projects
    @GetMapping("/{username}/projects")
    public ResponseEntity<List<ProjectDTO>> getProjects(@PathVariable String username) {
        return ResponseEntity.ok(projectService.getPublicProjects(username));
    }

    // GET /api/users/{username}/projects/{slug}
    @GetMapping("/{username}/projects/{slug}")
    public ResponseEntity<ProjectDTO> getProjectDetail(
            @PathVariable String username,
            @PathVariable String slug) {
        return ResponseEntity.ok(projectService.getPublicProject(username, slug));
    }

    // ============================================================
    // BLOG
    // ============================================================

    // GET /api/users/{username}/blog
    @GetMapping("/{username}/blog")
    public ResponseEntity<List<BlogPostDTO>> getBlogList(@PathVariable String username) {
        return ResponseEntity.ok(blogService.getPublishedByUser(username));
    }

    // GET /api/users/{username}/blog/featured
    @GetMapping("/{username}/blog/featured")
    public ResponseEntity<List<BlogPostDTO>> getFeaturedBlog(@PathVariable String username) {
        return ResponseEntity.ok(blogService.getFeaturedByUser(username));
    }

    // GET /api/users/{username}/blog/{slug}
    @GetMapping("/{username}/blog/{slug}")
    public ResponseEntity<BlogPostDTO> getBlogDetail(
            @PathVariable String username,
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublicBySlug(username, slug));
    }

    // ============================================================
    // SKILLS
    // ============================================================

    // GET /api/users/{username}/skills
    @GetMapping("/{username}/skills")
    public ResponseEntity<Map<String, Map<String, Object>>> getSkills(@PathVariable String username) {
        return ResponseEntity.ok(skillService.getPublicGrouped(username));
    }

    // ============================================================
    // EXPERIENCES
    // ============================================================

    // GET /api/users/{username}/experiences
    @GetMapping("/{username}/experiences")
    public ResponseEntity<List<ExperienceDTO>> getExperiences(@PathVariable String username) {
        return ResponseEntity.ok(experienceService.getPublicList(username));
    }

    // ============================================================
    // TECH STACK
    // ============================================================

    // GET /api/users/{username}/tech-stack
    @GetMapping("/{username}/tech-stack")
    public ResponseEntity<List<TechStackDTO>> getTechStack(@PathVariable String username) {
        return ResponseEntity.ok(techStackService.getPublicList(username));
    }

    // ============================================================
    // CONTACT FORM (PUBLIC)
    // ============================================================

    // POST /api/users/{username}/messages
    @PostMapping("/{username}/messages")
    public ResponseEntity<MessageDTO> submitMessage(
            @PathVariable String username,
            @Valid @RequestBody MessageDTO dto) {
        dto.setRead(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(username, dto));
    }
}