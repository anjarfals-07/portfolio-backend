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
    private final ThemeService themeService;

    // ⭐ BARU — inject 2 service ini
    private final WorkExperienceService workExperienceService;
    private final EducationService educationService;

    // ============================================================
    // LIST ALL USERS (LANDING PAGE)
    // ============================================================

    @GetMapping
    public ResponseEntity<List<UserPublicDTO>> listAllUsers() {
        return ResponseEntity.ok(userService.getAllPublicUsers());
    }

    // ============================================================
    // PROFILE
    // ============================================================

    @GetMapping("/{username}")
    public ResponseEntity<ProfileDTO> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(profileService.getPublicProfile(username));
    }

    // ============================================================
    // THEME
    // ============================================================

    @GetMapping("/{username}/theme")
    public ResponseEntity<ThemeDTO> getPublicTheme(@PathVariable String username) {
        ThemeDTO theme = themeService.getPublicTheme(username);
        if (theme == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(theme);
    }

    // ============================================================
    // PROJECTS
    // ============================================================

    @GetMapping("/{username}/projects")
    public ResponseEntity<List<ProjectDTO>> getProjects(@PathVariable String username) {
        return ResponseEntity.ok(projectService.getPublicProjects(username));
    }

    @GetMapping("/{username}/projects/{slug}")
    public ResponseEntity<ProjectDTO> getProjectDetail(
            @PathVariable String username,
            @PathVariable String slug) {
        return ResponseEntity.ok(projectService.getPublicProject(username, slug));
    }

    // ============================================================
    // BLOG
    // ============================================================

    @GetMapping("/{username}/blog")
    public ResponseEntity<List<BlogPostDTO>> getBlogList(@PathVariable String username) {
        return ResponseEntity.ok(blogService.getPublishedByUser(username));
    }

    @GetMapping("/{username}/blog/featured")
    public ResponseEntity<List<BlogPostDTO>> getFeaturedBlog(@PathVariable String username) {
        return ResponseEntity.ok(blogService.getFeaturedByUser(username));
    }

    @GetMapping("/{username}/blog/{slug}")
    public ResponseEntity<BlogPostDTO> getBlogDetail(
            @PathVariable String username,
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublicBySlug(username, slug));
    }

    // ============================================================
    // SKILLS
    // ============================================================

    @GetMapping("/{username}/skills")
    public ResponseEntity<Map<String, Map<String, Object>>> getSkills(@PathVariable String username) {
        return ResponseEntity.ok(skillService.getPublicGrouped(username));
    }

    // ============================================================
    // EXPERIENCES (achievement/timeline)
    // ============================================================

    @GetMapping("/{username}/experiences")
    public ResponseEntity<List<ExperienceDTO>> getExperiences(@PathVariable String username) {
        return ResponseEntity.ok(experienceService.getPublicList(username));
    }

    // ============================================================
    // ⭐ WORK EXPERIENCES (riwayat pekerjaan) — BARU
    // ============================================================

    @GetMapping("/{username}/work-experiences")
    public ResponseEntity<List<WorkExperienceDTO>> getWorkExperiences(
            @PathVariable String username) {
        return ResponseEntity.ok(workExperienceService.listPublic(username));
    }

    // ============================================================
    // ⭐ EDUCATIONS (riwayat pendidikan) — BARU
    // ============================================================

    @GetMapping("/{username}/educations")
    public ResponseEntity<List<EducationDTO>> getEducations(
            @PathVariable String username) {
        return ResponseEntity.ok(educationService.listPublic(username));
    }

    // ============================================================
    // TECH STACK
    // ============================================================

    @GetMapping("/{username}/tech-stack")
    public ResponseEntity<List<TechStackDTO>> getTechStack(@PathVariable String username) {
        return ResponseEntity.ok(techStackService.getPublicList(username));
    }

    // ============================================================
    // CONTACT FORM (PUBLIC)
    // ============================================================

    @PostMapping("/{username}/messages")
    public ResponseEntity<MessageDTO> submitMessage(
            @PathVariable String username,
            @Valid @RequestBody MessageDTO dto) {
        dto.setRead(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(username, dto));
    }
}