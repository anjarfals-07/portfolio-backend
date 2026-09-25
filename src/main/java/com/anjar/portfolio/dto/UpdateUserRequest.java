package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Format email tidak valid")
    @Size(max = 200)
    private String email;

    @Size(max = 100)
    private String displayName;

    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug cuma boleh lowercase, angka, dan dash")
    private String portfolioSlug;

    private String role;
    private Boolean active;
}