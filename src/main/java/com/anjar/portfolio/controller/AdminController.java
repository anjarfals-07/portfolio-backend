package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.service.*;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Super Admin API.
 * Base path: /api/admin
 * Requires role SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminController {

    private final UserService userService;
    private final ProjectService projectService;
    private final BlogService blogService;
    private final ProfileService profileService;

    // ============================================================
    // USERS
    // ============================================================

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<List<UserAdminDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllForAdmin());
    }

    // GET /api/admin/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<UserAdminDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getByIdForAdmin(id));
    }

    // POST /api/admin/users
    @PostMapping("/users")
    public ResponseEntity<UserAdminDTO> createUser(
            @Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createByAdmin(req));
    }

    // PUT /api/admin/users/{id}
    @PutMapping("/users/{id}")
    public ResponseEntity<UserAdminDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateByAdmin(id, req));
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteByAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/admin/users/{id}/disable?disable=true
    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<UserAdminDTO> disableUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean disable) {
        return ResponseEntity.ok(userService.setActive(id, !disable));
    }

    // PATCH /api/admin/users/{id}/role?role=SUPER_ADMIN
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserAdminDTO> changeRole(
            @PathVariable Long id,
            @RequestParam String role) {
        return ResponseEntity.ok(userService.changeRole(id, role));
    }

    // ============================================================
    // APPROVAL (BARU — Fase 6)
    // ============================================================

    /**
     * Approve user — ubah status jadi ACTIVE.
     *
     * Setelah approve, user bisa login & akses dashboard.
     *
     * PATCH /api/admin/users/{id}/approve
     */
    @PatchMapping("/users/{id}/approve")
    public ResponseEntity<UserAdminDTO> approveUser(@PathVariable Long id) {
        Long adminId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.approveUser(id, adminId));
    }

    /**
     * Reject user — ubah status jadi REJECTED dengan alasan.
     *
     * PATCH /api/admin/users/{id}/reject?reason=xxx
     */
    @PatchMapping("/users/{id}/reject")
    public ResponseEntity<UserAdminDTO> rejectUser(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long adminId = SecurityUtil.requireCurrentUserId();
        String finalReason = (reason != null && !reason.isBlank())
                ? reason
                : "Tidak memenuhi syarat";
        return ResponseEntity.ok(userService.rejectUser(id, adminId, finalReason));
    }

    /**
     * Suspend user — ubah status jadi SUSPENDED dengan alasan.
     *
     * PATCH /api/admin/users/{id}/suspend?reason=xxx
     */
    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<UserAdminDTO> suspendUser(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        Long adminId = SecurityUtil.requireCurrentUserId();
        String finalReason = (reason != null && !reason.isBlank())
                ? reason
                : "Melanggar aturan";
        return ResponseEntity.ok(userService.suspendUser(id, adminId, finalReason));
    }

    // ============================================================
    // USER DATA (VIEW ONLY)
    // ============================================================

    // GET /api/admin/users/{id}/projects
    @GetMapping("/users/{id}/projects")
    public ResponseEntity<List<ProjectDTO>> getUserProjects(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getAllProjects(id));
    }

    // GET /api/admin/users/{id}/blog
    @GetMapping("/users/{id}/blog")
    public ResponseEntity<List<BlogPostDTO>> getUserBlog(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getByUserIdForAdmin(id));
    }

    // GET /api/admin/users/{id}/profile
    @GetMapping("/users/{id}/profile")
    public ResponseEntity<ProfileDTO> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }

    // ============================================================
    // STATS
    // ============================================================

    // GET /api/admin/stats
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {
        return ResponseEntity.ok(userService.getAdminStats());
    }
}