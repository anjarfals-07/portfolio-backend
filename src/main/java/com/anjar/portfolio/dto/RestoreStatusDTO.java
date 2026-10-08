package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Status proses restore (untuk polling dari FE).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreStatusDTO {

    private String phase;

    private boolean running;

    private int percent;

    private String message;

    private LocalDateTime updatedAt;
}