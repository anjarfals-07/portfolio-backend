package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDTO {

    private Long id;

    @NotBlank(message = "Title wajib diisi")
    @Size(max = 500, message = "Title maksimal 500 karakter")
    private String title;

    @Size(max = 500, message = "Slug maksimal 500 karakter")
    private String slug;

    @Size(max = 5000, message = "Excerpt maksimal 5000 karakter")
    private String excerpt;

    private String content;

    private String coverUrl;

    private List<String> tags;

    private Integer readingTime;

    private Boolean published;

    private Boolean featured;

    private Long viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}