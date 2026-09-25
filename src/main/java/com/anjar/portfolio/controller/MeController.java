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

    // ============================================================
    // ME (CURRENT USER INFO)
    // ============================================================

    // GET /api/me
    @GetMapping
    public ResponseEntity<UserMeDTO> getMe() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.getMe(userId));
    }

    // ============================================================
    // PROFILE
    // ============================================================

    // GET /api/me/profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getProfile() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    // PUT /api/me/profile
    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }

    // ============================================================
    // PROJECTS
    // ============================================================

    // GET /api/me/projects
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDTO>> getMyProjects() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getAllProjects(userId));
    }

    // GET /api/me/projects/{id}
    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectDTO> getMyProject(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.getProjectById(id, userId));
    }

    // POST /api/me/projects
    @PostMapping("/projects")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(userId, dto));
    }

    // PUT /api/me/projects/{id}
    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(projectService.updateProject(id, userId, dto));
    }

    // DELETE /api/me/projects/{id}
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        projectService.deleteProject(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // BLOG
    // ============================================================

    // GET /api/me/blog
    @GetMapping("/blog")
    public ResponseEntity<List<BlogPostDTO>> getMyBlog() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getAllForOwner(userId));
    }

    // GET /api/me/blog/{id}
    @GetMapping("/blog/{id}")
    public ResponseEntity<BlogPostDTO> getMyBlogPost(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getByIdForOwner(id, userId));
    }

    // POST /api/me/blog
    @PostMapping("/blog")
    public ResponseEntity<BlogPostDTO> createBlog(@Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.create(userId, req));
    }

    // PUT /api/me/blog/{id}
    @PutMapping("/blog/{id}")
    public ResponseEntity<BlogPostDTO> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.update(id, userId, req));
    }

    // DELETE /api/me/blog/{id}
    @DeleteMapping("/blog/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        blogService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SKILLS
    // ============================================================

    // GET /api/me/skills
    @GetMapping("/skills")
    public ResponseEntity<List<SkillDTO>> getMySkills() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getAll(userId));
    }

    // GET /api/me/skills/grouped
    @GetMapping("/skills/grouped")
    public ResponseEntity<Map<String, Map<String, Object>>> getMySkillsGrouped() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.getGrouped(userId));
    }

    // POST /api/me/skills
    @PostMapping("/skills")
    public ResponseEntity<SkillDTO> createSkill(@Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(skillService.create(userId, dto));
    }

    // PUT /api/me/skills/{id}
    @PutMapping("/skills/{id}")
    public ResponseEntity<SkillDTO> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(skillService.update(id, userId, dto));
    }

    // DELETE /api/me/skills/{id}
    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        skillService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // EXPERIENCES
    // ============================================================

    // GET /api/me/experiences
    @GetMapping("/experiences")
    public ResponseEntity<List<ExperienceDTO>> getMyExperiences() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getAll(userId));
    }

    // GET /api/me/experiences/{id}
    @GetMapping("/experiences/{id}")
    public ResponseEntity<ExperienceDTO> getMyExperience(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.getById(id, userId));
    }

    // POST /api/me/experiences
    @PostMapping("/experiences")
    public ResponseEntity<ExperienceDTO> createExperience(@Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienceService.create(userId, dto));
    }

    // PUT /api/me/experiences/{id}
    @PutMapping("/experiences/{id}")
    public ResponseEntity<ExperienceDTO> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(experienceService.update(id, userId, dto));
    }

    // DELETE /api/me/experiences/{id}
    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        experienceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // TECH STACK
    // ============================================================

    // GET /api/me/tech-stack
    @GetMapping("/tech-stack")
    public ResponseEntity<List<TechStackDTO>> getMyTechStack() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getAll(userId));
    }

    // GET /api/me/tech-stack/{id}
    @GetMapping("/tech-stack/{id}")
    public ResponseEntity<TechStackDTO> getMyTechStackItem(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.getById(id, userId));
    }

    // POST /api/me/tech-stack
    @PostMapping("/tech-stack")
    public ResponseEntity<TechStackDTO> createTechStack(@Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(techStackService.create(userId, dto));
    }

    // PUT /api/me/tech-stack/{id}
    @PutMapping("/tech-stack/{id}")
    public ResponseEntity<TechStackDTO> updateTechStack(
            @PathVariable Long id,
            @Valid @RequestBody TechStackDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(techStackService.update(id, userId, dto));
    }

    // DELETE /api/me/tech-stack/{id}
    @DeleteMapping("/tech-stack/{id}")
    public ResponseEntity<Void> deleteTechStack(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        techStackService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // MESSAGES (INBOX)
    // ============================================================

    // GET /api/me/messages
    @GetMapping("/messages")
    public ResponseEntity<List<MessageDTO>> getMyMessages() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getAll(userId));
    }

    // GET /api/me/messages/unread
    @GetMapping("/messages/unread")
    public ResponseEntity<List<MessageDTO>> getMyUnreadMessages() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getUnread(userId));
    }

    // GET /api/me/messages/count-unread
    @GetMapping("/messages/count-unread")
    public ResponseEntity<Map<String, Long>> countMyUnread() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(userId)));
    }

    // GET /api/me/messages/{id}
    @GetMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> getMyMessage(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getById(id, userId));
    }

    // PATCH /api/me/messages/{id}/read
    @PatchMapping("/messages/{id}/read")
    public ResponseEntity<MessageDTO> markMyMessageAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean read) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.markAsRead(id, userId, read));
    }

    // DELETE /api/me/messages/{id}
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMyMessage(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        messageService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}