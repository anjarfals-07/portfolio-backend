package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ThemeDTO;
import com.anjar.portfolio.security.CurrentUserId;   // ⭐ Ganti import
import com.anjar.portfolio.service.ThemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Theme Controller — endpoint public + owner (multi-user).
 *
 * Base path: /api/themes
 *
 * ⭐ FIX: Pakai @CurrentUserId (custom annotation) untuk inject user ID
 * via CurrentUserIdResolver → SecurityUtil.requireCurrentUserId()
 * yang support 2 tipe principal (UserDetailsImpl & CurrentUser).
 */
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ThemeController {

    private final ThemeService themeService;    

    // ============================================================
    // PRESETS (public)
    // ============================================================

    @GetMapping("/presets")
    public ResponseEntity<List<Map<String, Object>>> getPresets() {
        return ResponseEntity.ok(themeService.getPresets());
    }

    // ============================================================
    // PUBLIC THEME — by username (no auth)
    // ============================================================

    @GetMapping("/public/{username}")
    public ResponseEntity<ThemeDTO> getPublicTheme(@PathVariable String username) {
        ThemeDTO theme = themeService.getPublicTheme(username);
        if (theme == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(theme);
    }

    // ============================================================
    // OWNER — GET MY THEME
    // ============================================================

    @GetMapping("/me")
    public ResponseEntity<ThemeDTO> getMyTheme(
            @CurrentUserId Long userId   // ⭐ FIX
    ) {
        ThemeDTO theme = themeService.getTheme(userId);
        if (theme == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(theme);
    }

    // ============================================================
    // OWNER — SAVE THEME (upsert)
    // ============================================================

    @PutMapping("/me")
    public ResponseEntity<ThemeDTO> saveMyTheme(
            @CurrentUserId Long userId,   // ⭐ FIX
            @Valid @RequestBody ThemeDTO dto
    ) {
        ThemeDTO saved = themeService.saveTheme(userId, dto);
        return ResponseEntity.ok(saved);
    }

    // ============================================================
    // OWNER — APPLY PRESET
    // ============================================================

    @PostMapping("/me/preset")
    public ResponseEntity<ThemeDTO> applyPreset(
            @CurrentUserId Long userId,   // ⭐ FIX
            @RequestBody Map<String, String> body
    ) {
        String presetName = body.get("preset");
        if (presetName == null || presetName.isBlank()) {
            throw new IllegalArgumentException("Field 'preset' wajib diisi");
        }
        ThemeDTO saved = themeService.applyPreset(userId, presetName);
        return ResponseEntity.ok(saved);
    }

    // ============================================================
    // OWNER — RESET THEME
    // ============================================================

    @DeleteMapping("/me")
    public ResponseEntity<Void> resetMyTheme(
            @CurrentUserId Long userId   // ⭐ FIX
    ) {
        themeService.resetTheme(userId);
        return ResponseEntity.noContent().build();
    }
}