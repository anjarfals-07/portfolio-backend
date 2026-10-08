package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Status koneksi & info database.
 * Dipakai di FE untuk nampilin status di tab Backup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseStatusDTO {

    /** Connected / Disconnected */
    private boolean connected;

    /** PostgreSQL version (mis. "16.4") */
    private String version;

    /** Nama database */
    private String databaseName;

    /** Host (tanpa kredensial!) */
    private String host;

    /** Port */
    private Integer port;

    /** Backup mode aktif: DOCKER | LOCAL */
    private String backupMode;

    /** Pesan error (kalau disconnected) */
    private String errorMessage;

    /** Kapan terakhir dicek */
    private LocalDateTime checkedAt;
}