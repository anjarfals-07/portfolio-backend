package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String portfolioSlug;
    private String role;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}