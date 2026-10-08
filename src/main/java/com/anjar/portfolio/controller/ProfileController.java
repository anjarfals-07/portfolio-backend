package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.service.ProfileService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ProfileController {

    private final ProfileService profileService;

    // ===== GET PROFILE (OWNER) =====
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    // ===== GET PUBLIC PROFILE =====
    @GetMapping("/public/{portfolioSlug}")
    public ResponseEntity<ProfileDTO> getPublicProfile(@PathVariable String portfolioSlug) {
        return ResponseEntity.ok(profileService.getPublicProfile(portfolioSlug));
    }

    // ===== SAVE / UPDATE PROFILE (OWNER) =====
    @PostMapping
    public ResponseEntity<ProfileDTO> saveProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }

    // ============================================================
    // ⭐ CV PREFERENCES — endpoint khusus
    // ============================================================

    /**
     * Update cvPreferences.
     *
     * Query params:
     *   ?merge=true  → gabung dengan existing (partial update)
     *   ?merge=false → replace total (default)
     *
     * Body kosong ({}) atau null → reset ke default (semua field null)
     */
    @PutMapping("/cv-preferences")
    public ResponseEntity<ProfileDTO> updateCvPreferences(
            @RequestBody(required = false) Map<String, Object> prefs,
            @RequestParam(value = "merge", defaultValue = "false") boolean merge) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.updateCvPreferences(userId, prefs, merge));
    }

    /**
     * Reset cvPreferences ke null (default sistem).
     */
    @DeleteMapping("/cv-preferences")
    public ResponseEntity<ProfileDTO> resetCvPreferences() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.updateCvPreferences(userId, null, false));
    }
}