package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response setelah restore selesai.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreResultDTO {

    private String status;

    private String filename;

    private long sizeBytes;

    private String safetyBackupFilename;

    private long durationMs;

    private int tablesRestored;

    private long userCount;

    private LocalDateTime restoredAt;

    private String message;
}