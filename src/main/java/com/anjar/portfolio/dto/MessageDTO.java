package com.anjar.portfolio.dto;

import jakarta.validation.constraints.Email;
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
public class MessageDTO {

    private Long id;

    @NotBlank(message = "Nama wajib diisi")
    @Size(min = 2, max = 100, message = "Nama minimal 2, maksimal 100 karakter")
    private String name;

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 200)
    private String email;

    @NotBlank(message = "Subjek wajib diisi")
    @Size(min = 3, max = 200, message = "Subjek minimal 3, maksimal 200 karakter")
    private String subject;

    @NotBlank(message = "Pesan wajib diisi")
    @Size(min = 10, max = 2000, message = "Pesan minimal 10, maksimal 2000 karakter")
    private String message;

    private Boolean read;

    private LocalDateTime createdAt;
}