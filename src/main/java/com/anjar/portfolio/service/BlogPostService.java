package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.BlogPostDTO;
import com.anjar.portfolio.entity.BlogPost;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.BlogPostRepository;
import com.anjar.portfolio.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;

    // ===== GET ALL PUBLISHED (public) =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getAllPublished() {
        return blogPostRepository.findByPublishedTrueOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET FEATURED =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getFeatured() {
        return blogPostRepository.findByFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET ALL (admin) =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getAllForAdmin() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET BY ID =====
    @Transactional(readOnly = true)
    public BlogPostDTO getById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));
        return toDTO(post);
    }

    // ===== GET BY SLUG (increment view) =====
    @Transactional
    public BlogPostDTO getBySlug(String slug) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "slug", slug));

        post.setViewCount(post.getViewCount() + 1);
        blogPostRepository.save(post);

        return toDTO(post);
    }

    // ===== CREATE =====
    @Transactional
    public BlogPostDTO create(BlogPostDTO dto) {
        String slug = (dto.getSlug() == null || dto.getSlug().isBlank())
                ? SlugUtil.toSlug(dto.getTitle())
                : SlugUtil.toSlug(dto.getSlug());

        String uniqueSlug = ensureUniqueSlug(slug, null);

        // Auto-calculate reading time (200 words/min)
        Integer readingTime = dto.getReadingTime();
        if (readingTime == null && dto.getContent() != null) {
            int wordCount = dto.getContent().split("\\s+").length;
            readingTime = Math.max(1, wordCount / 200);
        }

        BlogPost post = BlogPost.builder()
                .title(dto.getTitle())
                .slug(uniqueSlug)
                .excerpt(dto.getExcerpt())
                .content(dto.getContent())
                .coverUrl(dto.getCoverUrl())
                .tags(dto.getTags())
                .readingTime(readingTime)
                .published(dto.getPublished() != null ? dto.getPublished() : true)
                .featured(dto.getFeatured() != null ? dto.getFeatured() : false)
                .build();

        return toDTO(blogPostRepository.save(post));
    }

    // ===== UPDATE =====
    @Transactional
    public BlogPostDTO update(Long id, BlogPostDTO dto) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            String slug = SlugUtil.toSlug(dto.getSlug());
            post.setSlug(ensureUniqueSlug(slug, id));
        }

        if (dto.getExcerpt() != null) post.setExcerpt(dto.getExcerpt());

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
            int wordCount = dto.getContent().split("\\s+").length;
            post.setReadingTime(Math.max(1, wordCount / 200));
        }

        if (dto.getCoverUrl() != null) post.setCoverUrl(dto.getCoverUrl());
        if (dto.getTags() != null) post.setTags(dto.getTags());
        if (dto.getReadingTime() != null) post.setReadingTime(dto.getReadingTime());
        if (dto.getPublished() != null) post.setPublished(dto.getPublished());
        if (dto.getFeatured() != null) post.setFeatured(dto.getFeatured());

        return toDTO(blogPostRepository.save(post));
    }

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        if (!blogPostRepository.existsById(id)) {
            throw new ResourceNotFoundException("BlogPost", id);
        }
        blogPostRepository.deleteById(id);
    }

    // ===== HELPERS =====
    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (blogPostRepository.existsBySlug(slug)) {
            if (excludeId != null) {
                BlogPost existing = blogPostRepository.findBySlug(slug).orElse(null);
                if (existing != null && existing.getId().equals(excludeId)) {
                    return slug;
                }
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
        return slug;
    }

    private BlogPostDTO toDTO(BlogPost p) {
        return BlogPostDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .slug(p.getSlug())
                .excerpt(p.getExcerpt())
                .content(p.getContent())
                .coverUrl(p.getCoverUrl())
                .tags(p.getTags())
                .readingTime(p.getReadingTime())
                .published(p.getPublished())
                .featured(p.getFeatured())
                .viewCount(p.getViewCount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}