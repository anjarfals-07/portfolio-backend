package com.anjar.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request untuk tambah custom domain.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDomainRequest {

    @NotBlank(message = "Domain tidak boleh kosong")
    @Size(max = 255, message = "Domain maksimal 255 karakter")
    @Pattern(
            regexp = "^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}$",
            message = "Format domain tidak valid (contoh: badru.com)"
    )
    private String domain;
}