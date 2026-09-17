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
public class ProjectDTO {

    private Long id;

    @NotBlank(message = "Title wajib diisi")
    @Size(max = 200, message = "Title maksimal 200 karakter")
    private String title;

    private String slug;  // auto-generate dari title kalau kosong

    @Size(max = 500, message = "Description maksimal 500 karakter")
    private String description;

    private String content;

    private String thumbnailUrl;

    private List<String> techStack;

    private String githubUrl;

    private String demoUrl;

    private Boolean featured;

    private Boolean published;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}