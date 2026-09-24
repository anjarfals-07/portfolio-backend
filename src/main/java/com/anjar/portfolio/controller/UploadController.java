package com.anjar.portfolio.controller;

import com.anjar.portfolio.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UploadController {

    private final UploadService uploadService;

    // ===== UPLOAD IMAGE =====
    // POST /api/upload/image
    // multipart/form-data dengan field "file"
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> result = uploadService.uploadImage(file);

        Map<String, Object> response = new HashMap<>();
        response.put("url", result.get("secure_url"));
        response.put("publicId", result.get("public_id"));
        response.put("width", result.get("width"));
        response.put("height", result.get("height"));
        response.put("format", result.get("format"));
        response.put("bytes", result.get("bytes"));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== DELETE IMAGE =====
    // DELETE /api/upload/image?publicId=xxx
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, String>> deleteImage(
            @RequestParam("publicId") String publicId) {
        uploadService.deleteImage(publicId);
        return ResponseEntity.ok(Map.of("message", "Gambar berhasil dihapus"));
    }
}