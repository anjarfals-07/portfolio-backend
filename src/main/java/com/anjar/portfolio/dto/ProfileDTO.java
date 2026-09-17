package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private Long id;

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 200, message = "Nama maksimal 200 karakter")
    private String fullName;

    @Size(max = 200)
    private String role;

    private String bio;

    @Size(max = 500)
    private String shortBio;

    @Email(message = "Format email tidak valid")
    private String email;

    @Size(max = 200)
    private String location;

    private String avatarUrl;

    private String cvUrl;

    private Boolean availableForWork;

    // [{"icon": "pi pi-github", "url": "...", "label": "GitHub"}]
    private List<Map<String, String>> socials;

    private LocalDateTime updatedAt;
}