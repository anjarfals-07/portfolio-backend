package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
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
public class TechStackDTO {

    private Long id;

    @NotBlank(message = "Nama tech wajib diisi")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String icon;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}