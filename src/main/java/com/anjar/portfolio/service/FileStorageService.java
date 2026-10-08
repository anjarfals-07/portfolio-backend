package com.anjar.portfolio.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final List<String> ALLOWED_MIME = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "application/pdf"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    // ============================================================
    // UPLOAD
    // ============================================================
    public String upload(MultipartFile file, String subfolder) {
        validate(file);

        try {
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + ext;
            String relativePath = subfolder + "/" + filename;

            Path targetDir = Paths.get(uploadDir, subfolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            Path targetFile = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/uploads/" + relativePath;
            log.info("✅ File uploaded: {}", url);
            return url;

        } catch (IOException e) {
            log.error("❌ Failed to upload file", e);
            throw new RuntimeException("Gagal upload file: " + e.getMessage());
        }
    }

    // ============================================================
    // DELETE
    // ============================================================
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String relativePath = fileUrl.replace(baseUrl + "/uploads/", "");
            Path file = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();

            if (Files.exists(file)) {
                Files.delete(file);
                log.info("🗑️ File deleted: {}", fileUrl);
            }
        } catch (IOException e) {
            log.warn("⚠️ Failed to delete file: {}", fileUrl, e);
        }
    }

    // ============================================================
    // VALIDASI
    // ============================================================
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File wajib diupload");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Ukuran file maksimal 5MB");
        }

        String mime = file.getContentType();
        if (mime == null || !ALLOWED_MIME.contains(mime.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipe file tidak diizinkan. Hanya JPG, PNG, WEBP, PDF."
            );
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}