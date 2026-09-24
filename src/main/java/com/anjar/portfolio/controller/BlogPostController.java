package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.BlogPostDTO;
import com.anjar.portfolio.service.BlogPostService;
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
public class BlogPostController {

    private final BlogPostService blogPostService;

    // ===== PUBLIC: GET ALL PUBLISHED =====
    @GetMapping
    public ResponseEntity<List<BlogPostDTO>> getAllPublished() {
        return ResponseEntity.ok(blogPostService.getAllPublished());
    }

    // ===== PUBLIC: GET FEATURED =====
    @GetMapping("/featured")
    public ResponseEntity<List<BlogPostDTO>> getFeatured() {
        return ResponseEntity.ok(blogPostService.getFeatured());
    }

    // ===== ADMIN: GET ALL =====
    @GetMapping("/all")
    public ResponseEntity<List<BlogPostDTO>> getAllForAdmin() {
        return ResponseEntity.ok(blogPostService.getAllForAdmin());
    }

    // ===== PUBLIC: GET BY SLUG (increment view) =====
    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogPostDTO> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogPostService.getBySlug(slug));
    }

    // ===== GET BY ID =====
    @GetMapping("/{id}")
    public ResponseEntity<BlogPostDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getById(id));
    }

    // ===== ADMIN: CREATE =====
    @PostMapping
    public ResponseEntity<BlogPostDTO> create(@Valid @RequestBody BlogPostDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(blogPostService.create(dto));
    }

    // ===== ADMIN: UPDATE =====
    @PutMapping("/{id}")
    public ResponseEntity<BlogPostDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostDTO dto) {
        return ResponseEntity.ok(blogPostService.update(id, dto));
    }

    // ===== ADMIN: DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        blogPostService.delete(id);
        return ResponseEntity.noContent().build();
    }
}