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
public class ExperienceDTO {

    private Long id;

    @Size(max = 100)
    private String year;

    @NotBlank(message = "Title wajib diisi")
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String subtitle;

    private String description;

    @Size(max = 100)
    private String icon;

    @Size(max = 20)
    private String color;

    private List<String> tags;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}