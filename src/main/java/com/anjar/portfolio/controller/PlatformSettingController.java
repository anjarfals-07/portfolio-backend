package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.PlatformSettingDTO;
import com.anjar.portfolio.service.PlatformSettingService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class PlatformSettingController {

    private final PlatformSettingService service;

    @GetMapping
    public ResponseEntity<PlatformSettingDTO> getSettings() {
        return ResponseEntity.ok(service.getSettings());
    }

    @PutMapping
    public ResponseEntity<PlatformSettingDTO> updateSettings(
            @Valid @RequestBody PlatformSettingDTO dto
    ) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(service.saveSettings(dto, userId));
    }
}