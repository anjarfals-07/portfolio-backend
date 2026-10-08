package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.BackupStreamDTO;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.BackupMode;
import com.anjar.portfolio.exception.BackupException;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final UserRepository userRepo;
    private final ActivityLogService activityLog;

    // ===== Config: General =====
    @Value("${app.backup.enabled:true}")
    private boolean enabled;

    @Value("${app.backup.mode:DOCKER}")
    private String modeRaw;

    @Value("${app.backup.temp-dir:backups}")
    private String tempDir;

    @Value("${app.backup.compress:true}")
    private boolean compress;

    @Value("${app.backup.timeout-seconds:300}")
    private int timeoutSeconds;

    @Value("${app.backup.filename-prefix:backup}")
    private String filenamePrefix;

    @Value("${app.backup.cleanup-after-stream:true}")
    private boolean cleanupAfterStream;

    @Value("${app.backup.cleanup-delay-minutes:60}")
    private int cleanupDelayMinutes;

    // ===== Config: DOCKER =====
    @Value("${app.backup.docker-container:portfolio-pg-client}")
    private String dockerContainer;

    @Value("${app.backup.docker-backup-dir:/backups}")
    private String dockerBackupDir;

    // ===== Config: LOCAL =====
    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    // ===== Config: Database =====
    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    private final AtomicBoolean running = new AtomicBoolean(false);

    // ============================================================
    // MAIN: Create backup (dengan adminId)
    // ============================================================
    public BackupStreamDTO createBackup(Long adminId) {
        if (!enabled) {
            throw new BackupException("Fitur backup sedang dinonaktifkan");
        }

        if (!running.compareAndSet(false, true)) {
            throw new BackupException("Backup sedang berjalan, coba lagi sebentar lagi");
        }

        long startTime = System.currentTimeMillis();
        User admin = adminId != null ? userRepo.findById(adminId).orElse(null) : null;

        try {
            BackupMode mode = BackupMode.valueOf(modeRaw.toUpperCase());
            String filename = generateFilename(mode);

            log.info("🔄 Starting backup (mode={}, filename={}, adminId={})",
                    mode, filename, adminId);

            File backupFile = (mode == BackupMode.DOCKER)
                    ? executeDockerBackup(filename)
                    : executeLocalBackup(filename);

            long duration = System.currentTimeMillis() - startTime;
            long sizeBytes = backupFile.length();

            log.info("✅ Backup completed in {}ms ({} bytes): {}",
                    duration, sizeBytes, backupFile.getAbsolutePath());

            // ⭐ Log activity
            if (admin != null) {
                activityLog.logBackupCreated(admin, backupFile.getName(), sizeBytes);
            }

            return BackupStreamDTO.builder()
                    .filename(backupFile.getName())
                    .sizeBytes(sizeBytes)
                    .contentType(compress ? "application/gzip" : "application/sql")
                    .createdAt(LocalDateTime.now())
                    .databaseName(extractDatabaseName())
                    .mode(mode.name())
                    .message("Backup berhasil dalam " + (duration / 1000) + " detik")
                    .build();

        } catch (BackupException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Backup failed", e);
            throw new BackupException("Backup gagal: " + e.getMessage(), e);
        } finally {
            running.set(false);
        }
    }

    // Overload tanpa adminId
    public BackupStreamDTO createBackup() {
        return createBackup(null);
    }

    // ============================================================
    // ⭐ LOG DOWNLOAD (dipanggil controller setelah stream)
    // ============================================================
    public void logDownload(Long adminId, String filename) {
        if (adminId == null) return;
        try {
            User admin = userRepo.findById(adminId).orElse(null);
            if (admin != null) {
                activityLog.logBackupDownloaded(admin, filename);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to log backup download: {}", e.getMessage());
        }
    }

    // ============================================================
    // DOCKER MODE
    // ============================================================
    private File executeDockerBackup(String filename) throws IOException, InterruptedException {
        Path hostPath = Paths.get(tempDir, filename).toAbsolutePath().normalize();
        Files.createDirectories(hostPath.getParent());

        String containerOutputPath = dockerBackupDir + "/" + filename;
        String connectionUrl = buildPostgresConnectionUrl();

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        command.add(dockerContainer);
        command.add("pg_dump");
        command.add(connectionUrl);
        command.add("-f");
        command.add(containerOutputPath);
        command.add("--no-owner");
        command.add("--no-privileges");

        log.info("🐳 Running: docker exec {} pg_dump <url> -f {}",
                dockerContainer, containerOutputPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new BackupException("Backup timeout setelah " + timeoutSeconds + " detik");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("❌ docker exec pg_dump failed (exit={}): {}", exitCode, output);
            throw new BackupException("pg_dump gagal: " + truncate(output, 500));
        }

        File backupFile = hostPath.toFile();
        if (!backupFile.exists()) {
            throw new BackupException("File backup tidak ditemukan: " + hostPath);
        }

        if (compress) {
            backupFile = compressFile(backupFile);
        }

        return backupFile;
    }

    // ============================================================
    // LOCAL MODE
    // ============================================================
    private File executeLocalBackup(String filename) throws IOException, InterruptedException {
        Path outputPath = Paths.get(tempDir, filename).toAbsolutePath().normalize();
        Files.createDirectories(outputPath.getParent());

        String host = extractHost();
        String port = extractPort();
        String dbName = extractDatabaseName();

        List<String> command = new ArrayList<>();
        command.add(pgDumpPath);
        command.add("-h");
        command.add(host);
        command.add("-p");
        command.add(port);
        command.add("-U");
        command.add(datasourceUsername);
        command.add("-d");
        command.add(dbName);
        command.add("-f");
        command.add(outputPath.toString());
        command.add("--no-owner");
        command.add("--no-privileges");

        log.info("💻 Running: {} -h {} -p {} -U {} -d {} -f {}",
                pgDumpPath, host, port, datasourceUsername, dbName, outputPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.environment().put("PGPASSWORD", datasourcePassword);

        Process process = pb.start();
        String output = readProcessOutput(process);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new BackupException("Backup timeout setelah " + timeoutSeconds + " detik");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("❌ pg_dump failed (exit={}): {}", exitCode, output);
            throw new BackupException("pg_dump gagal: " + truncate(output, 500));
        }

        File backupFile = outputPath.toFile();
        if (!backupFile.exists()) {
            throw new BackupException("File backup tidak ditemukan: " + outputPath);
        }

        if (compress) {
            backupFile = compressFile(backupFile);
        }

        return backupFile;
    }

    // ============================================================
    // COMPRESS
    // ============================================================
    private File compressFile(File input) throws IOException {
        Path gzPath = Paths.get(input.getAbsolutePath() + ".gz");

        try (FileInputStream fis = new FileInputStream(input);
             FileOutputStream fos = new FileOutputStream(gzPath.toFile());
             GZIPOutputStream gzip = new GZIPOutputStream(fos)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzip.write(buffer, 0, len);
            }
        }

        Files.deleteIfExists(input.toPath());

        log.info("🗜️ Compressed: {} ({} bytes)", gzPath.getFileName(), Files.size(gzPath));
        return gzPath.toFile();
    }

    // ============================================================
    // GET FILE
    // ============================================================
    public File getBackupFile(String filename) {
        validateFilename(filename);

        Path path = Paths.get(tempDir, filename).toAbsolutePath().normalize();
        File file = path.toFile();

        if (!file.exists() || !file.isFile()) {
            throw new BackupException("File backup tidak ditemukan: " + filename);
        }

        return file;
    }

    // ============================================================
    // DELETE
    // ============================================================
    public void deleteBackupFile(String filename) {
        if (!cleanupAfterStream) {
            log.debug("Cleanup disabled, skip delete: {}", filename);
            return;
        }

        try {
            validateFilename(filename);
            Path path = Paths.get(tempDir, filename).toAbsolutePath().normalize();
            if (Files.deleteIfExists(path)) {
                log.info("🗑️ Deleted temp backup: {}", filename);
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to delete backup file: {}", filename, e);
        }
    }

    // ============================================================
    // CLEANUP
    // ============================================================
    public int cleanupOrphanFiles() {
        Path dir = Paths.get(tempDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) return 0;

        int count = 0;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cleanupDelayMinutes);

        try (var stream = Files.list(dir)) {
            for (Path file : stream.toList()) {
                if (!Files.isRegularFile(file)) continue;

                String name = file.getFileName().toString();
                boolean shouldClean =
                        name.startsWith(filenamePrefix)
                                || name.startsWith("before-restore-")
                                || name.startsWith("restore-upload-")
                                || name.startsWith("restore-");
                if (!shouldClean) continue;

                LocalDateTime modified = LocalDateTime.ofInstant(
                        Files.getLastModifiedTime(file).toInstant(),
                        java.time.ZoneId.systemDefault()
                );

                if (modified.isBefore(cutoff)) {
                    Files.deleteIfExists(file);
                    log.info("🧹 Cleaned orphan: {}", name);
                    count++;
                }
            }
        } catch (IOException e) {
            log.error("❌ Cleanup failed", e);
        }

        return count;
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private String generateFilename(BackupMode mode) {
        String ts = LocalDateTime.now().format(TS_FMT);
        return filenamePrefix + "-" + ts + ".sql";
    }

    private String buildPostgresConnectionUrl() {
        String url = datasourceUrl;

        if (url.startsWith("jdbc:")) {
            url = url.substring("jdbc:".length());
        }

        if (!url.contains("@")) {
            String prefix = "postgresql://";
            if (url.startsWith(prefix)) {
                String rest = url.substring(prefix.length());
                return prefix + datasourceUsername + ":" + datasourcePassword + "@" + rest;
            }
        }

        return url;
    }

    private String extractHost() {
        String url = datasourceUrl;
        String afterScheme = url.substring(url.indexOf("://") + 3);

        int atIdx = afterScheme.indexOf('@');
        if (atIdx >= 0) afterScheme = afterScheme.substring(atIdx + 1);

        int colonIdx = afterScheme.indexOf(':');
        int slashIdx = afterScheme.indexOf('/');
        int end = (colonIdx > 0 && colonIdx < slashIdx) ? colonIdx : slashIdx;

        return afterScheme.substring(0, end);
    }

    private String extractPort() {
        String url = datasourceUrl;
        String afterScheme = url.substring(url.indexOf("://") + 3);

        int atIdx = afterScheme.indexOf('@');
        if (atIdx >= 0) afterScheme = afterScheme.substring(atIdx + 1);

        int colonIdx = afterScheme.indexOf(':');
        int slashIdx = afterScheme.indexOf('/');

        if (colonIdx < 0 || colonIdx > slashIdx) return "5432";
        return afterScheme.substring(colonIdx + 1, slashIdx);
    }

    private String extractDatabaseName() {
        String url = datasourceUrl;
        int lastSlash = url.lastIndexOf('/');
        String dbAndQuery = url.substring(lastSlash + 1);
        int queryIdx = dbAndQuery.indexOf('?');
        return queryIdx > 0 ? dbAndQuery.substring(0, queryIdx) : dbAndQuery;
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private void validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BackupException("Filename tidak boleh kosong");
        }
        String regex = "^" + filenamePrefix + "-\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.sql(\\.gz)?$";
        if (!filename.matches(regex)) {
            log.warn("⚠️ Invalid filename: {}", filename);
            throw new BackupException("Format filename tidak valid");
        }
    }
}