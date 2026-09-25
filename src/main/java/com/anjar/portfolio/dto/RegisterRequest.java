package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username wajib diisi")
    @Size(min = 3, max = 50, message = "Username 3-50 karakter")
    @Pattern(
            regexp = "^[a-z0-9_-]+$",
            message = "Username cuma boleh lowercase, angka, dash, underscore"
    )
    private String username;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 200)
    private String email;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, max = 100, message = "Password minimal 8 karakter")
    private String password;

    @Size(max = 100)
    private String displayName;

    // Optional: custom slug (kalau kosong, auto-generate)
    @Size(min = 3, max = 50)
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Slug cuma boleh lowercase, angka, dan dash"
    )
    private String portfolioSlug;
}