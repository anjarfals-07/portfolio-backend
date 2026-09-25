package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.service.ProfileService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ===== UPDATE PROFILE (OWNER — alias PUT) =====
    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(profileService.saveProfile(userId, dto));
    }
}