package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.UploadResult;
import com.anjar.portfolio.service.UploadService;
import com.anjar.portfolio.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class UploadController {

    private final UploadService uploadService;

    // POST /api/me/upload
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.requireCurrentUserId();
        UploadResult result = uploadService.uploadImage(file, userId);
        return ResponseEntity.ok(result);
    }

    // DELETE /api/me/upload?publicId=xxx
    @DeleteMapping
    public ResponseEntity<Void> deleteImage(@RequestParam String publicId) {
        Long userId = SecurityUtil.requireCurrentUserId();
        uploadService.deleteImage(publicId, userId);
        return ResponseEntity.noContent().build();
    }
}