package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {

    private Long id;

    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String categoryIcon;

    @NotBlank(message = "Nama skill wajib diisi")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Level wajib diisi")
    @Min(value = 0, message = "Level minimal 0")
    @Max(value = 100, message = "Level maksimal 100")
    private Integer level;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}