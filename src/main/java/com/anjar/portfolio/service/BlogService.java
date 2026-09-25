package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.AuthorDTO;
import com.anjar.portfolio.dto.BlogPostDTO;
import com.anjar.portfolio.dto.BlogPostRequest;
import com.anjar.portfolio.entity.BlogPost;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.BlogPostRepository;
import com.anjar.portfolio.repository.ProfileRepository;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // ===== OWNER: GET ALL (by userId) =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getAllForOwner(Long userId) {
        return blogPostRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ===== OWNER: GET BY ID =====
    @Transactional(readOnly = true)
    public BlogPostDTO getByIdForOwner(Long id, Long userId) {
        BlogPost post = findOwned(id, userId);
        return toDTO(post);
    }

    // ===== PUBLIC: GET PUBLISHED (by portfolioSlug) =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getPublishedByUser(String portfolioSlug) {
        return blogPostRepository
                .findByUserPortfolioSlugAndPublishedTrueOrderByCreatedAtDesc(portfolioSlug)
                .stream().map(this::toDTOWithAuthor).toList();
    }

    // ===== PUBLIC: GET FEATURED =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getFeaturedByUser(String portfolioSlug) {
        return blogPostRepository
                .findByUserPortfolioSlugAndFeaturedTrueAndPublishedTrueOrderByCreatedAtDesc(portfolioSlug)
                .stream().map(this::toDTOWithAuthor).toList();
    }

    // ===== PUBLIC: GET BY SLUG =====
    @Transactional
    public BlogPostDTO getPublicBySlug(String portfolioSlug, String slug) {
        BlogPost post = blogPostRepository
                .findByUserPortfolioSlugAndSlugAndPublishedTrue(portfolioSlug, slug)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", "slug", slug));

        // Increment view count
        post.setViewCount(post.getViewCount() + 1);
        blogPostRepository.save(post);

        return toDTOWithAuthor(post);
    }

    // ===== OWNER: CREATE =====
    @Transactional
    public BlogPostDTO create(Long userId, BlogPostRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String slug = (req.getSlug() == null || req.getSlug().isBlank())
                ? SlugUtil.toSlug(req.getTitle())
                : SlugUtil.toSlug(req.getSlug());

        String uniqueSlug = ensureUniqueSlug(userId, slug, null);

        BlogPost post = BlogPost.builder()
                .user(user)
                .title(req.getTitle())
                .slug(uniqueSlug)
                .excerpt(req.getExcerpt())
                .content(req.getContent())
                .coverUrl(req.getCoverUrl())
                .tags(req.getTags())
                .readingTime(req.getReadingTime() != null ? req.getReadingTime() : estimateReadingTime(req.getContent()))
                .published(req.getPublished() != null ? req.getPublished() : false)
                .featured(req.getFeatured() != null ? req.getFeatured() : false)
                .viewCount(0)
                .build();

        return toDTO(blogPostRepository.save(post));
    }

    // ===== OWNER: UPDATE =====
    @Transactional
    public BlogPostDTO update(Long id, Long userId, BlogPostRequest req) {
        BlogPost post = findOwned(id, userId);

        if (req.getTitle() != null) post.setTitle(req.getTitle());

        if (req.getSlug() != null && !req.getSlug().isBlank()) {
            String slug = SlugUtil.toSlug(req.getSlug());
            post.setSlug(ensureUniqueSlug(userId, slug, id));
        }

        if (req.getExcerpt() != null) post.setExcerpt(req.getExcerpt());
        if (req.getContent() != null) {
            post.setContent(req.getContent());
            if (req.getReadingTime() == null) {
                post.setReadingTime(estimateReadingTime(req.getContent()));
            }
        }
        if (req.getCoverUrl() != null) post.setCoverUrl(req.getCoverUrl());
        if (req.getTags() != null) post.setTags(req.getTags());
        if (req.getReadingTime() != null) post.setReadingTime(req.getReadingTime());
        if (req.getPublished() != null) post.setPublished(req.getPublished());
        if (req.getFeatured() != null) post.setFeatured(req.getFeatured());

        return toDTO(blogPostRepository.save(post));
    }

    // ===== OWNER: DELETE =====
    @Transactional
    public void delete(Long id, Long userId) {
        BlogPost post = findOwned(id, userId);
        blogPostRepository.delete(post);
    }

    // ===== SUPER ADMIN: GET ALL =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getAllForAdmin() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== SUPER ADMIN: GET BY USER ID =====
    @Transactional(readOnly = true)
    public List<BlogPostDTO> getByUserIdForAdmin(Long userId) {
        return blogPostRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ===== HELPER: Find owned post =====
    private BlogPost findOwned(Long id, Long userId) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BlogPost", id));

        if (!post.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Blog post ini bukan milik kamu");
        }
        return post;
    }

    // ===== HELPER: Unique slug per user =====
    private String ensureUniqueSlug(Long userId, String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (blogPostRepository.existsByUserIdAndSlug(userId, slug)) {
            if (excludeId != null) {
                BlogPost existing = blogPostRepository.findByUserIdAndSlug(userId, slug).orElse(null);
                if (existing != null && existing.getId().equals(excludeId)) {
                    return slug;
                }
            }
            counter++;
            slug = baseSlug + "-" + counter;
        }
        return slug;
    }

    // ===== HELPER: Estimate reading time (200 kata/menit) =====
    private Integer estimateReadingTime(String content) {
        if (content == null || content.isBlank()) return 1;
        int words = content.trim().split("\\s+").length;
        return Math.max(1, words / 200);
    }

    // ===== Mapper =====
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

    private BlogPostDTO toDTOWithAuthor(BlogPost p) {
        BlogPostDTO dto = toDTO(p);

        // Ambil author info dari User + Profile
        User user = p.getUser();
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);

        AuthorDTO author = AuthorDTO.builder()
                .name(profile != null && profile.getFullName() != null
                        ? profile.getFullName()
                        : user.getDisplayName())
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .role(profile != null ? profile.getRole() : null)
                .email(profile != null ? profile.getEmail() : user.getEmail())
                .location(profile != null ? profile.getLocation() : null)
                .bio(profile != null ? profile.getShortBio() : null)
                .build();

        dto.setAuthor(author);
        return dto;
    }
}