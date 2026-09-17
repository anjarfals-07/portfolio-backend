package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.service.ProfileService;
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

    // ===== GET PROFILE =====
    // GET /api/profile
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {
        return ResponseEntity.ok(profileService.getProfile());
    }

    // ===== SAVE / UPDATE PROFILE =====
    // POST /api/profile  (upsert: create kalau belum ada, update kalau udah ada)
    @PostMapping
    public ResponseEntity<ProfileDTO> saveProfile(@Valid @RequestBody ProfileDTO dto) {
        return ResponseEntity.ok(profileService.saveProfile(dto));
    }

    // ===== UPDATE PROFILE (alias PUT) =====
    // PUT /api/profile
    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody ProfileDTO dto) {
        return ResponseEntity.ok(profileService.saveProfile(dto));
    }
}