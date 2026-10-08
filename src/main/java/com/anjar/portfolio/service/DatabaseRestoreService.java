package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.RestoreResultDTO;
import com.anjar.portfolio.dto.RestoreStatusDTO;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.RestorePhase;
import com.anjar.portfolio.exception.RestoreException;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseRestoreService {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final long MAX_FILE_SIZE = 500L * 1024 * 1024;

    private final UserRepository userRepo;
    private final ActivityLogService activityLog;

    // ===== Config =====
    @Value("${app.backup.temp-dir:backups}")
    private String tempDir;

    @Value("${app.backup.docker-container:portfolio-pg-client}")
    private String dockerContainer;

    @Value("${app.backup.docker-backup-dir:/backups}")
    private String dockerBackupDir;

    @Value("${app.backup.timeout-seconds:300}")
    private int timeoutSeconds;

    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile RestoreStatusDTO currentStatus = RestoreStatusDTO.builder()
            .phase(RestorePhase.DONE.name())
            .running(false)
            .percent(0)
            .message("Idle")
            .updatedAt(LocalDateTime.now())
            .build();

    // ============================================================
    // MAIN: Restore (dengan adminId)
    // ============================================================
    @Transactional
    public RestoreResultDTO restoreFromFile(MultipartFile file, Long adminId) {
        if (!running.compareAndSet(false, true)) {
            throw new RestoreException("Restore sedang berjalan, coba lagi nanti");
        }

        long startTime = System.currentTimeMillis();
        User admin = adminId != null ? userRepo.findById(adminId).orElse(null) : null;
        String originalFilename = file.getOriginalFilename();

        // ⭐ Log start
        if (admin != null) {
            activityLog.logRestoreStarted(admin, originalFilename, file.getSize());
        }

        try {
            updateStatus(RestorePhase.VALIDATING, 5, "Memvalidasi file...");
            validateFile(file);

            File uploadedFile = saveUploadedFile(file);
            File sqlFile = extractGzipIfNeeded(uploadedFile);

            updateStatus(RestorePhase.BACKUP_CURRENT, 15, "Backup DB saat ini (safety net)...");
            File safetyBackup = autoBackupBeforeRestore();
            log.info("✅ Safety backup created: {}", safetyBackup.getName());

            updateStatus(RestorePhase.RESTORING, 40, "Restore database...");

            String containerFilename = "restore-" + System.currentTimeMillis() + ".sql";
            String containerPath = dockerBackupDir + "/" + containerFilename;
            copyFileToContainer(sqlFile, containerFilename);
            executeRestoreInContainer(containerPath);

            updateStatus(RestorePhase.VERIFYING, 85, "Verifikasi hasil restore...");

            int tablesRestored = countTables();
            long userCount = countUsers();

            updateStatus(RestorePhase.DONE, 100, "Restore berhasil!");

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Restore completed in {}ms — {} tables, {} users",
                    duration, tablesRestored, userCount);

            // ⭐ Log complete
            if (admin != null) {
                activityLog.logRestoreCompleted(
                        admin, originalFilename, duration, tablesRestored, userCount);
            }

            return RestoreResultDTO.builder()
                    .status("SUCCESS")
                    .filename(originalFilename)
                    .sizeBytes(file.getSize())
                    .safetyBackupFilename(safetyBackup.getName())
                    .durationMs(duration)
                    .tablesRestored(tablesRestored)
                    .userCount(userCount)
                    .restoredAt(LocalDateTime.now())
                    .message("Restore berhasil. Silakan logout dan login ulang.")
                    .build();

        } catch (RestoreException e) {
            updateStatus(RestorePhase.FAILED, 0, e.getMessage());

            // ⭐ Log failed
            if (admin != null) {
                activityLog.logRestoreFailed(admin, originalFilename, e.getMessage());
            }
            throw e;

        } catch (Exception e) {
            updateStatus(RestorePhase.FAILED, 0, "Restore gagal: " + e.getMessage());
            log.error("❌ Restore failed", e);

            if (admin != null) {
                activityLog.logRestoreFailed(admin, originalFilename, e.getMessage());
            }
            throw new RestoreException("Restore gagal: " + e.getMessage(), e);

        } finally {
            running.set(false);
        }
    }

    public RestoreResultDTO restoreFromFile(MultipartFile file) {
        return restoreFromFile(file, null);
    }

    // ============================================================
    // STATUS
    // ============================================================
    public RestoreStatusDTO getStatus() {
        return currentStatus;
    }

    public boolean isRunning() {
        return running.get();
    }

    // ============================================================
    // VALIDASI
    // ============================================================
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RestoreException("File tidak boleh kosong");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RestoreException(
                    "File terlalu besar. Max: " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new RestoreException("Nama file tidak valid");
        }

        String lower = filename.toLowerCase();
        if (!lower.endsWith(".sql") && !lower.endsWith(".sql.gz") && !lower.endsWith(".gz")) {
            throw new RestoreException("Format file harus .sql, .sql.gz, atau .gz");
        }
    }

    // ============================================================
    // SIMPAN FILE UPLOAD
    // ============================================================
    private File saveUploadedFile(MultipartFile file) throws IOException {
        Path tempPath = Paths.get(tempDir).toAbsolutePath().normalize();
        Files.createDirectories(tempPath);

        String filename = "restore-upload-" + System.currentTimeMillis() + ".sql";
        Path targetPath = tempPath.resolve(filename);

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("📁 Uploaded file saved: {}", targetPath);
        return targetPath.toFile();
    }

    // ============================================================
    // EXTRACT GZIP
    // ============================================================
    private File extractGzipIfNeeded(File input) throws IOException {
        if (!isGzipFile(input)) {
            log.info("📄 File bukan gzip, langsung pakai: {}", input.getName());
            return input;
        }

        Path outputPath = Paths.get(input.getAbsolutePath() + ".extracted.sql");

        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(input));
             FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        log.info("📦 Extracted gzip: {} → {}",
                input.getName(), outputPath.getFileName());

        Files.deleteIfExists(input.toPath());
        return outputPath.toFile();
    }

    private boolean isGzipFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] magic = new byte[2];
            if (fis.read(magic) != 2) return false;
            return (magic[0] & 0xFF) == 0x1F && (magic[1] & 0xFF) == 0x8B;
        } catch (IOException e) {
            return false;
        }
    }

    // ============================================================
    // AUTO BACKUP
    // ============================================================
    private File autoBackupBeforeRestore() throws IOException, InterruptedException {
        String filename = "before-restore-" + LocalDateTime.now().format(TS_FMT) + ".sql";
        Path outputPath = Paths.get(tempDir, filename).toAbsolutePath().normalize();
        Files.createDirectories(outputPath.getParent());

        String connectionUrl = buildPostgresConnectionUrl();
        String containerPath = dockerBackupDir + "/" + filename;

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        command.add(dockerContainer);
        command.add("pg_dump");
        command.add(connectionUrl);
        command.add("-f");
        command.add(containerPath);
        command.add("--no-owner");
        command.add("--no-privileges");

        log.info("🛡️ Creating safety backup: {}", filename);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RestoreException("Safety backup timeout");
        }

        if (process.exitValue() != 0) {
            throw new RestoreException("Safety backup gagal: " + truncate(output, 500));
        }

        File backupFile = outputPath.toFile();
        if (!backupFile.exists()) {
            throw new RestoreException("Safety backup file tidak ditemukan");
        }

        return backupFile;
    }

    // ============================================================
    // COPY KE CONTAINER
    // ============================================================
    private void copyFileToContainer(File localFile, String containerFilename)
            throws IOException, InterruptedException {

        String containerTarget = dockerContainer + ":" + dockerBackupDir + "/" + containerFilename;

        List<String> command = List.of(
                "docker", "cp",
                localFile.getAbsolutePath(),
                containerTarget
        );

        log.info("📤 Copying to container: {}", containerTarget);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);
        process.waitFor(30, TimeUnit.SECONDS);

        if (process.exitValue() != 0) {
            throw new RestoreException("Copy ke container gagal: " + truncate(output, 500));
        }
    }

    // ============================================================
    // EXECUTE RESTORE
    // ============================================================
    private void executeRestoreInContainer(String containerPath)
            throws IOException, InterruptedException {

        String connectionUrl = buildPostgresConnectionUrl();

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("exec");
        command.add(dockerContainer);
        command.add("psql");
        command.add(connectionUrl);
        command.add("-f");
        command.add(containerPath);

        log.info("🔄 Executing restore: psql -f {}", containerPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = readProcessOutput(process);

        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RestoreException("Restore timeout setelah " + timeoutSeconds + " detik");
        }

        if (output.contains("ERROR:")) {
            log.warn("⚠️ Restore psql output has errors: {}", truncate(output, 1000));
            long errorCount = output.lines().filter(l -> l.contains("ERROR:")).count();
            if (errorCount > 10) {
                throw new RestoreException(
                        "Restore gagal dengan " + errorCount + " error. " + truncate(output, 500));
            }
        }

        log.info("✅ psql restore completed");
    }

    // ============================================================
    // VERIFY
    // ============================================================
    private int countTables() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", dockerContainer,
                    "psql", buildPostgresConnectionUrl(),
                    "-t", "-c",
                    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = readProcessOutput(process);
            process.waitFor(10, TimeUnit.SECONDS);

            String[] lines = output.trim().split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d+")) return Integer.parseInt(line);
            }
            return 0;
        } catch (Exception e) {
            log.warn("⚠️ Failed to count tables: {}", e.getMessage());
            return 0;
        }
    }

    private long countUsers() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", dockerContainer,
                    "psql", buildPostgresConnectionUrl(),
                    "-t", "-c",
                    "SELECT count(*) FROM users"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = readProcessOutput(process);
            process.waitFor(10, TimeUnit.SECONDS);

            String[] lines = output.trim().split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d+")) return Long.parseLong(line);
            }
            return 0;
        } catch (Exception e) {
            log.warn("⚠️ Failed to count users: {}", e.getMessage());
            return 0;
        }
    }

    // ============================================================
    // STATUS UPDATE
    // ============================================================
    private void updateStatus(RestorePhase phase, int percent, String message) {
        currentStatus = RestoreStatusDTO.builder()
                .phase(phase.name())
                .running(phase != RestorePhase.DONE && phase != RestorePhase.FAILED)
                .percent(percent)
                .message(message)
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("📍 [{}] {}% — {}", phase, percent, message);
    }

    // ============================================================
    // HELPERS
    // ============================================================
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
}