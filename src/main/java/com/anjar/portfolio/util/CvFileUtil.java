package com.anjar.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Utility untuk manage file CV di filesystem.
 *
 * Konvensi nama file:
 *   Generated: cv-{userId}-gen-{timestamp}.pdf
 *   Uploaded:  cv-{userId}-upload-{uuid}.pdf
 */
@Slf4j
public final class CvFileUtil {

    private CvFileUtil() {}

    // ============================================================
    // PREFIXES
    // ============================================================
    public static final String PREFIX_GENERATED = "gen-";
    public static final String PREFIX_UPLOAD    = "upload-";

    // ============================================================
    // BUILD FILENAME
    // ============================================================
    public static String buildGeneratedFilename(Long userId) {
        return "cv-" + userId + "-" + PREFIX_GENERATED
                + System.currentTimeMillis() + ".pdf";
    }

    public static String buildUploadFilename(Long userId, String uuid) {
        return "cv-" + userId + "-" + PREFIX_UPLOAD + uuid + ".pdf";
    }

    // ============================================================
    // DELETE — by prefix tertentu
    // ============================================================
    /**
     * Hapus file CV user dengan prefix tertentu.
     *
     * @param userId     user id
     * @param cvDir      direktori CV
     * @param kindPrefix PREFIX_GENERATED / PREFIX_UPLOAD
     */
    public static void deleteByKind(Long userId, Path cvDir, String kindPrefix)
            throws IOException {
        if (!Files.exists(cvDir)) return;

        String prefix = "cv-" + userId + "-" + kindPrefix;

        try (Stream<Path> stream = Files.list(cvDir)) {
            stream.filter(p -> p.getFileName().toString().startsWith(prefix))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            log.info("🗑️ Deleted {}: {}", kindPrefix, p.getFileName());
                        } catch (IOException e) {
                            log.warn("Failed delete {}: {}", p, e.getMessage());
                        }
                    });
        }
    }

    // ============================================================
    // ⭐ DELETE — SEMUA CV USER (upload + generated)
    // ============================================================
    /**
     * Hapus SEMUA file CV user (baik upload maupun generated).
     *
     * Dipakai saat upload / generate CV baru — biar cuma 1 CV aktif
     * per user (tidak numpuk file di disk).
     *
     * @param userId user id
     * @param cvDir  direktori CV
     */
    public static void deleteAllByUser(Long userId, Path cvDir) throws IOException {
        deleteByKind(userId, cvDir, PREFIX_UPLOAD);
        deleteByKind(userId, cvDir, PREFIX_GENERATED);
    }

    // ============================================================
    // SECURITY CHECK — file milik user?
    // ============================================================
    /**
     * Cek apakah file dimiliki user (berdasarkan prefix `cv-{userId}-`).
     *
     * Terima BOTH upload & generated — karena prefixnya sama.
     * Contoh valid untuk userId=5:
     *   - cv-5-upload-abc-123.pdf       ✅
     *   - cv-5-gen-1727345678901.pdf    ✅
     *   - cv-99-upload-xxx.pdf          ❌ (user lain)
     *   - random-file.pdf                ❌
     */
    public static boolean isOwnedBy(Long userId, String filename) {
        if (filename == null || filename.isBlank()) return false;
        return filename.startsWith("cv-" + userId + "-");
    }
}