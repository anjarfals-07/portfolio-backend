package com.anjar.portfolio.controller.admin;

import com.anjar.portfolio.dto.BackupStreamDTO;
import com.anjar.portfolio.dto.DatabaseStatusDTO;
import com.anjar.portfolio.dto.RestoreResultDTO;
import com.anjar.portfolio.dto.RestoreStatusDTO;
import com.anjar.portfolio.exception.RestoreException;
import com.anjar.portfolio.service.DatabaseBackupService;
import com.anjar.portfolio.service.DatabaseRestoreService;
import com.anjar.portfolio.service.DatabaseStatusService;
import com.anjar.portfolio.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminBackupController {

    private final DatabaseBackupService backupService;
    private final DatabaseRestoreService restoreService;
    private final DatabaseStatusService statusService;

    // ============================================================
    // STATUS
    // ============================================================
    @GetMapping("/database/status")
    public ResponseEntity<DatabaseStatusDTO> getDatabaseStatus() {
        log.info("📊 Database status check requested");
        return ResponseEntity.ok(statusService.checkStatus());
    }

    // ============================================================
    // BACKUP & STREAM
    // ============================================================
    @PostMapping("/backup/stream")
    public ResponseEntity<Resource> streamBackup() {
        log.info("🔄 Backup request received");

        Long adminId = SecurityUtil.getCurrentUserIdOrNull();

        BackupStreamDTO metadata = backupService.createBackup(adminId);

        log.info("📤 Streaming backup: {} ({} bytes)",
                metadata.getFilename(), metadata.getSizeBytes());

        File backupFile = backupService.getBackupFile(metadata.getFilename());
        Resource resource = new FileSystemResource(backupFile);

        String encodedFilename = encodeFilename(metadata.getFilename());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + metadata.getFilename() + "\"; " +
                        "filename*=UTF-8''" + encodedFilename);
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.add("X-Backup-Filename", metadata.getFilename());
        headers.add("X-Backup-Size", String.valueOf(metadata.getSizeBytes()));
        headers.add("X-Backup-Mode", metadata.getMode());
        headers.add("X-Backup-Database", metadata.getDatabaseName());
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                "X-Backup-Filename,X-Backup-Size,X-Backup-Mode,X-Backup-Database,Content-Disposition");

        // ⭐ Log download
        backupService.logDownload(adminId, metadata.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .contentLength(metadata.getSizeBytes())
                .body(new DeletingFileSystemResource(backupFile, backupService));
    }

    // ============================================================
    // RESTORE
    // ============================================================
    @PostMapping(value = "/backup/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestoreResultDTO> restoreBackup(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("🔄 Restore request received: {} ({} bytes)",
                file.getOriginalFilename(), file.getSize());

        validateFileBasic(file);

        Long adminId = SecurityUtil.getCurrentUserIdOrNull();

        RestoreResultDTO result = restoreService.restoreFromFile(file, adminId);

        log.info("✅ Restore completed: {}", result.getMessage());

        return ResponseEntity.ok(result);
    }

    // ============================================================
    // RESTORE STATUS
    // ============================================================
    @GetMapping("/backup/restore/status")
    public ResponseEntity<RestoreStatusDTO> getRestoreStatus() {
        return ResponseEntity.ok(restoreService.getStatus());
    }

    // ============================================================
    // HELPER
    // ============================================================
    private void validateFileBasic(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RestoreException("File tidak boleh kosong");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RestoreException("Nama file tidak valid");
        }

        String lower = filename.toLowerCase();
        if (!lower.endsWith(".sql")
                && !lower.endsWith(".sql.gz")
                && !lower.endsWith(".gz")) {
            throw new RestoreException("Format file harus .sql, .sql.gz, atau .gz");
        }
    }

    private String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            return filename;
        }
    }

    // ============================================================
    // INNER CLASS
    // ============================================================
    private static class DeletingFileSystemResource extends FileSystemResource {

        private final DatabaseBackupService service;
        private final String filename;
        private boolean deleted = false;

        public DeletingFileSystemResource(File file, DatabaseBackupService service) {
            super(file);
            this.service = service;
            this.filename = file.getName();
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            java.io.InputStream is = super.getInputStream();
            return new java.io.FilterInputStream(is) {
                @Override
                public void close() throws java.io.IOException {
                    try {
                        super.close();
                    } finally {
                        if (!deleted) {
                            deleted = true;
                            new Thread(() -> {
                                try {
                                    Thread.sleep(500);
                                    service.deleteBackupFile(filename);
                                } catch (Exception e) {
                                    // ignore
                                }
                            }).start();
                        }
                    }
                }
            };
        }
    }
}