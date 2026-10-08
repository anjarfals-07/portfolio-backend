package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.service.*;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Private API — owner CRUD data sendiri.
 * Base path: /api/me
 * Requires auth (OWNER atau SUPER_ADMIN).
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class MeController {

    private final UserService userService;
    private final ProfileService profileService;
    private final ProjectService projectService;
    private final BlogService blogService;
    private final SkillService skillService;
    private final ExperienceService experienceService;
    private final TechStackService techStackService;
    private final MessageService messageService;
    private final ThemeService themeService;   // ← TAMBAH (Fase 7.7)

    // ============================================================
    // ME (CURRENT USER INFO)
    // ============================================================

    @GetMapping
    public ResponseEntity<UserMeDTO> getMe() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.getMe(userId));
    }

    // ============================================================
    // PROFILE
    // ============================================================

    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    /**
     * FULL UPDATE — wajib kirim fullName (karena @Valid).
     * Dipakai untuk save form lengkap di ManageProfile.
     */
    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }

    /**
     * ⭐ PARTIAL UPDATE — kirim field spesifik aja tanpa wajib fullName.
     * Dipakai untuk save Personal Info (religion, maritalStatus, dll).
     *
     * Tidak pakai @Valid → tidak butuh fullName.
     */
    @PatchMapping("/profile")
    public ResponseEntity<ProfileDTO> patchProfile(@RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }

    // ============================================================
    // THEME (BARU — Fase 7.7)
    // ============================================================

    /**
     * GET /api/me/theme
     * Get theme user sendiri.
     * Kalau belum ada → 204 No Content.
     */
    @GetMapping("/theme")
    public ResponseEntity<ThemeDTO> getMyTheme() {
        Long userId = SecurityUtil.requireCurrentUserId();
        ThemeDTO theme = themeService.getTheme(userId);
        if (theme == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(theme);
    }

    /**
     * PUT /api/me/theme
     * Save/update theme user sendiri (upsert).
     */
    @PutMapping("/theme")
    public ResponseEntity<ThemeDTO> saveMyTheme(@Valid @RequestBody ThemeDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(themeService.saveTheme(userId, dto));
    }

    /**
     * POST /api/me/theme/apply/{preset}
     * Apply preset theme.
     */
    @PostMapping("/theme/apply/{preset}")
    public ResponseEntity<ThemeDTO> applyPreset(@PathVariable String preset) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(themeService.applyPreset(userId, preset));
    }

    /**
     * DELETE /api/me/theme
     * Reset theme (hapus theme, balik ke default).
     */
    @DeleteMapping("/theme")
    public ResponseEntity<Void> resetMyTheme() {
        Long userId = SecurityUtil.requireCurrentUserId();
        themeService.resetTheme(userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // PROJECTS
    // ============================================================

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDTO>> getMyProjects() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getAllProjects(userId));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectDTO> getMyProject(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    @PostMapping("/projects")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(userId, dto));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.updateProject(id, userId, dto));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // BLOG
    // ============================================================

    @GetMapping("/blog")
    public ResponseEntity<List<BlogPostDTO>> getMyBlog() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getAllForOwner(userId));
    }

    @GetMapping("/blog/{id}")
    public ResponseEntity<BlogPostDTO> getMyBlogPost(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getByIdForOwner(id, userId));
    }

    @PostMapping("/blog")
    public ResponseEntity<BlogPostDTO> createBlog(@Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.create(userId, req));
    }

    @PutMapping("/blog/{id}")
    public ResponseEntity<BlogPostDTO> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.update(id, userId, req));
    }

    @DeleteMapping("/blog/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        blogService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SKILLS
    // ============================================================

    @GetMapping("/skills")
    public ResponseEntity<List<SkillDTO>> getMySkills() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getAll(userId));
    }

    @GetMapping("/skills/grouped")
    public ResponseEntity<Map<String, Map<String, Object>>> getMySkillsGrouped() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getGrouped(userId));
    }

    @PostMapping("/skills")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skillService.create(userId, dto));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<SkillDTO> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.update(id, userId, dto));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        skillService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // EXPERIENCES
    // ============================================================

    @GetMapping("/experiences")
    public ResponseEntity<List<ExperienceDTO>> getMyExperiences() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getAll(userId));
    }

    @GetMapping("/experiences/{id}")
    public ResponseEntity<ExperienceDTO> getMyExperience(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getById(id, userId));
    }

    @PostMapping("/experiences")
    public ResponseEntity<ExperienceDTO> createExperience(@Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienceService.create(userId, dto));
    }

    @PutMapping("/experiences/{id}")
    public ResponseEntity<ExperienceDTO> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.update(id, userId, dto));
    }

    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        experienceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // TECH STACK
    // ============================================================

    @GetMapping("/tech-stack")
    public ResponseEntity<List<TechStackDTO>> getMyTechStack() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getAll(userId));
    }

    @GetMapping("/tech-stack/{id}")
    public ResponseEntity<TechStackDTO> getMyTechStackItem(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getById(id, userId));
    }

    @PostMapping("/tech-stack")
    public ResponseEntity<TechStackDTO> createTechStack(@Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(techStackService.create(userId, dto));
    }

    @PutMapping("/tech-stack/{id}")
    public ResponseEntity<TechStackDTO> updateTechStack(
            @PathVariable Long id,
            @Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.update(id, userId, dto));
    }

    @DeleteMapping("/tech-stack/{id}")
    public ResponseEntity<Void> deleteTechStack(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        techStackService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // MESSAGES (INBOX)
    // ============================================================

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMyMessages() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getAll(userId));
    }

    @GetMapping("/messages/unread")
    public ResponseEntity<List<MessageDTO>> getMyUnreadMessages() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getUnread(userId));
    }

    @GetMapping("/messages/count-unread")
    public ResponseEntity<Map<String, Long>> countMyUnread() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(userId)));
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> getMyMessage(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getById(id, userId));
    }

    @PatchMapping("/messages/{id}/read")
    public ResponseEntity<MessageDTO> markMyMessageAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean read) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.markAsRead(id, userId, read));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMyMessage(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        messageService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}