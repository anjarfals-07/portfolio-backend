package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.BlogPostDTO;
import com.anjar.portfolio.dto.BlogPostRequest;
import com.anjar.portfolio.service.BlogService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class BlogController {

    private final BlogService blogService;

    // ============================================================
    // PUBLIC ENDPOINTS (by portfolioSlug)
    // ============================================================

    // GET /api/blog/user/{portfolioSlug}
    @GetMapping("/user/{portfolioSlug}")
    public ResponseEntity<List<BlogPostDTO>> getPublishedByUser(
            @PathVariable String portfolioSlug) {
        return ResponseEntity.ok(blogService.getPublishedByUser(portfolioSlug));
    }

    // GET /api/blog/user/{portfolioSlug}/featured
    @GetMapping("/user/{portfolioSlug}/featured")
    public ResponseEntity<List<BlogPostDTO>> getFeaturedByUser(
            @PathVariable String portfolioSlug) {
        return ResponseEntity.ok(blogService.getFeaturedByUser(portfolioSlug));
    }

    // GET /api/blog/user/{portfolioSlug}/slug/{slug}
    @GetMapping("/user/{portfolioSlug}/slug/{slug}")
    public ResponseEntity<BlogPostDTO> getPublicBySlug(
            @PathVariable String portfolioSlug,
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublicBySlug(portfolioSlug, slug));
    }

    // ============================================================
    // OWNER ENDPOINTS (requires auth, /api/blog/me/...)
    // ============================================================

    // GET /api/blog/me
    @GetMapping("/me")
    public ResponseEntity<List<BlogPostDTO>> getAllForOwner() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getAllForOwner(userId));
    }

    // GET /api/blog/me/{id}
    @GetMapping("/me/{id}")
    public ResponseEntity<BlogPostDTO> getByIdForOwner(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.getByIdForOwner(id, userId));
    }

    // POST /api/blog/me
    @PostMapping("/me")
    public ResponseEntity<BlogPostDTO> create(@Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogService.create(userId, req));
    }

    // PUT /api/blog/me/{id}
    @PutMapping("/me/{id}")
    public ResponseEntity<BlogPostDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest req) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(blogService.update(id, userId, req));
    }

    // DELETE /api/blog/me/{id}
    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        blogService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // SUPER ADMIN ENDPOINTS
    // NOTE: Endpoint admin blog dipindah ke AdminController
    //       (/api/admin/users/{id}/blog)
    //       Hapus endpoint duplikat di bawah biar gak bingung.
    // ============================================================

    // HAPUS: /api/blog/admin/all
    // HAPUS: /api/blog/admin/user/{userId}
    // Alasan: udah ada di AdminController sebagai /api/admin/users/{id}/blog
}