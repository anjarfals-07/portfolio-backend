package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long userId;
    private String username;
    private String role;         // ← BARU: OWNER / SUPER_ADMIN
    private String portfolioSlug; // ← BARU: /anjar
    private String displayName;   // ← BARU
    private Long expiresIn;       // ← BARU: ms
}