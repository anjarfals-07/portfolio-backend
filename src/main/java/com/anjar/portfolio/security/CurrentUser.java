package com.anjar.portfolio.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Principal object — dipasang oleh JwtAuthFilter.
 *
 * Dipakai controller + SecurityUtil untuk ambil user data
 * tanpa query DB lagi.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {

    private Long id;
    private String username;
    private String role;
}