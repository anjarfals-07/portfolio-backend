package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Metadata backup yang di-stream ke FE.
 * Dipakai untuk header response & info ke user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupStreamDTO {

    /** Nama file backup (mis. backup-2026-01-15_14-30-22.sql.gz) */
    private String filename;

    /** Ukuran file dalam bytes */
    private long sizeBytes;

    /** MIME type: application/gzip atau application/sql */
    private String contentType;

    /** Waktu file dibuat */
    private LocalDateTime createdAt;

    /** Nama DB yang di-backup */
    private String databaseName;

    /** Mode yang dipakai: DOCKER | LOCAL */
    private String mode;

    /** Info tambahan (untuk log) */
    private String message;
}