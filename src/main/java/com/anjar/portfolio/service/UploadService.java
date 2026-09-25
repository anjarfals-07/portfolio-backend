package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.UploadResult;
import com.anjar.portfolio.exception.ForbiddenException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder:portfolio}")
    private String folder;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public UploadResult uploadImage(MultipartFile file, Long userId) {
        // Validasi
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Ukuran file maksimal 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Format file tidak didukung. Cuma boleh: JPG, PNG, WEBP, GIF"
            );
        }

        try {
            String userFolder = folder + "/user-" + userId;

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", userFolder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true,
                            "overwrite", false
                    )
            );

            log.info("✅ Uploaded image for user {}: {}",
                    userId, uploadResult.get("secure_url"));

            return UploadResult.builder()
                    .url((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .format((String) uploadResult.get("format"))
                    .bytes(((Number) uploadResult.get("bytes")).longValue())
                    .build();

        } catch (IOException e) {
            log.error("❌ Upload failed: {}", e.getMessage());
            throw new RuntimeException("Gagal upload image: " + e.getMessage(), e);
        }
    }

    public void deleteImage(String publicId, Long userId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Public ID tidak boleh kosong");
        }

        // Validasi ownership
        String expectedPrefix = folder + "/user-" + userId + "/";
        if (!publicId.startsWith(expectedPrefix)) {
            throw new ForbiddenException(
                    "Kamu gak punya akses untuk hapus image ini"
            );
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );

            String status = (String) result.get("result");
            log.info("🗑️ Delete image {}: {}", publicId, status);

            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new RuntimeException("Gagal hapus image: " + status);
            }

        } catch (IOException e) {
            log.error("❌ Delete failed: {}", e.getMessage());
            throw new RuntimeException("Gagal hapus image: " + e.getMessage(), e);
        }
    }
}