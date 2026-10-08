package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.UploadResult;
import com.anjar.portfolio.service.ProfileService;
import com.anjar.portfolio.util.CvFileUtil;
import com.anjar.portfolio.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Controller untuk upload & delete CV.
 *
 * Endpoint:
 *   POST   /api/me/upload/cv    → upload CV PDF manual
 *   DELETE /api/me/upload/cv    → hapus CV (upload ATAU generated)
 *
 * File disimpan di folder `uploads/cv/`.
 * URL publik: {base-url}/uploads/cv/{filename}
 *
 * ⭐ Endpoint delete menerima BOTH upload dan generated file —
 *    karena user hanya punya 1 CV aktif.
 */
@Slf4j
@RestController
@RequestMapping("/api/me/upload/cv")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class FileUploadController {

    private final ProfileService profileService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.upload.cv.max-size-mb:10}")
    private int maxSizeMb;

    // ============================================================
    // UPLOAD CV
    // ============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResult> uploadCv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Long userId = SecurityUtil.requireCurrentUserId();

        // ===== 1. Validasi kosong =====
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong");
        }

        // ===== 2. Validasi tipe (content-type + extension) =====
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (contentType == null || !contentType.equals("application/pdf")
                || originalFilename == null
                || !originalFilename.toLowerCase().endsWith(".pdf")) {
            log.warn("❌ Invalid file: contentType={}, filename={}",
                    contentType, originalFilename);
            throw new IllegalArgumentException("File harus berformat PDF");
        }

        // ===== 3. Validasi size =====
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "File maksimal " + maxSizeMb + "MB");
        }

        // ===== 4. Ensure dir =====
        Path cvDir = Paths.get(uploadDir, "cv").toAbsolutePath().normalize();
        Files.createDirectories(cvDir);

        // ===== 5. Hapus SEMUA CV lama user (upload + generated) =====
        // Karena cuma boleh 1 CV aktif per user
        CvFileUtil.deleteAllByUser(userId, cvDir);

        // ===== 6. Save file =====
        String filename = CvFileUtil.buildUploadFilename(
                userId, UUID.randomUUID().toString()
        );
        Path targetPath = cvDir.resolve(filename);

        // Path traversal guard (safety)
        if (!targetPath.normalize().startsWith(cvDir)) {
            throw new SecurityException("Invalid target path");
        }

        Files.copy(file.getInputStream(), targetPath,
                StandardCopyOption.REPLACE_EXISTING);

        log.info("✅ CV uploaded for user {}: {} ({} bytes)",
                userId, filename, file.getSize());

        // ===== 7. Build URL =====
        String fileUrl = baseUrl + "/uploads/cv/" + filename;

        // ===== 8. Update profile (transactional) =====
        profileService.updateCvAfterUpload(userId, fileUrl);

        // ===== 9. Return =====
        return ResponseEntity.ok(UploadResult.builder()
                .url(fileUrl)
                .publicId(filename)
                .build());
    }

    // ============================================================
    // DELETE CV (upload ATAU generated)
    // ============================================================
    @DeleteMapping
    public ResponseEntity<Void> deleteCv(
            @RequestParam("publicId") String publicId
    ) throws IOException {

        Long userId = SecurityUtil.requireCurrentUserId();

        // ===== 1. Validasi =====
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId wajib diisi");
        }

        // ===== 2. Normalize: ambil filename saja =====
        // Handle kalau frontend kirim:
        //   - "cv-5-gen-xxx.pdf"                     (filename)
        //   - "http://.../uploads/cv/cv-5-gen-xxx.pdf" (URL penuh)
        String filename = publicId;
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        if (filename.contains("?")) {
            filename = filename.substring(0, filename.indexOf("?"));
        }

        log.info("🗑️ Delete CV request: userId={}, publicId={}, normalized={}",
                userId, publicId, filename);

        // ===== 3. Security: cek ownership =====
        // ⭐ Terima SEMUA file dengan prefix "cv-{userId}-"
        //    Baik upload (`upload-`) maupun generated (`gen-`)
        if (!CvFileUtil.isOwnedBy(userId, filename)) {
            log.warn("⚠️ User {} attempted to delete file not owned: {}",
                    userId, filename);
            throw new SecurityException("Kamu tidak berhak menghapus file ini");
        }

        // ===== 4. Hapus file fisik =====
        Path cvDir = Paths.get(uploadDir, "cv").toAbsolutePath().normalize();
        Path filePath = cvDir.resolve(filename).normalize();

        // Path traversal guard
        if (!filePath.startsWith(cvDir)) {
            throw new SecurityException("Invalid path");
        }

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("✅ CV file deleted for user {}: {}", userId, filename);
        } else {
            log.warn("⚠️ File not found: {}", filePath);
        }

        // ===== 5. Clear profile (transactional) =====
        profileService.clearCvIfMatch(
                userId, baseUrl + "/uploads/cv/" + filename
        );

        return ResponseEntity.noContent().build();
    }
}