package com.anjar.portfolio.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.folder:portfolio}")
    private String folder;

    // ===== UPLOAD GAMBAR =====
    public Map<String, Object> uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "allowed_formats", "jpg,jpeg,png,webp,gif",
                            "transformation", "q_auto,f_auto"
                    )
            );

            log.info("Image uploaded: {}", uploadResult.get("secure_url"));
            return uploadResult;

        } catch (IOException e) {
            log.error("Upload failed", e);
            throw new RuntimeException("Gagal upload gambar: " + e.getMessage());
        }
    }

    // ===== DELETE GAMBAR =====
    public void deleteImage(String publicId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
            log.info("Image deleted: {} ({})", publicId, result.get("result"));
        } catch (IOException e) {
            log.error("Delete failed", e);
            throw new RuntimeException("Gagal hapus gambar: " + e.getMessage());
        }
    }

    // ===== VALIDASI =====
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File kosong");
        }

        // Max 10MB
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Ukuran file maksimal 10MB");
        }

        // Cek content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File harus berupa gambar");
        }
    }
}