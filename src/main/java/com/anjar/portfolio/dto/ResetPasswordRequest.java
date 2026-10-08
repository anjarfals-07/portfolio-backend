package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token wajib diisi")
    private String token;

    @NotBlank(message = "Password baru wajib diisi")
    @Size(min = 8, max = 100, message = "Password minimal 8 karakter")
    private String newPassword;
}