package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BlogPostRequest {

    @NotBlank(message = "Title wajib diisi")
    @Size(max = 300)
    private String title;

    @Size(max = 300)
    private String slug;

    private String excerpt;

    private String content;

    @Size(max = 500)
    private String coverUrl;

    private List<String> tags;

    private Integer readingTime;

    private Boolean published;

    private Boolean featured;
}