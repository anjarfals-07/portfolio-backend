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
    @Size(max = 300)
    private String title;

    private String slug;  // auto-generate kalau kosong

    private String excerpt;

    private String content;

    private String coverUrl;

    private List<String> tags;

    private Integer readingTime;

    private Boolean published;

    private Boolean featured;

    private Integer viewCount;

    // ===== AUTHOR INFO (untuk public response) =====
    private AuthorDTO author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}