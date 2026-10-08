package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ProfileRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service untuk manage Profile.
 *
 * Fitur:
 * - Get private profile (by userId)
 * - Get public profile (by portfolioSlug)
 * - Save / update profile (create kalau belum ada)
 * - Update cvPreferences (partial/merge)
 * - Reset cvPreferences
 * - Update cvUrl setelah upload/generate
 *
 * Field yang di-handle:
 * - Identitas: fullName, role, bio, shortBio
 * - Kontak: email, phone, city, location
 * - ⭐ Personal: religion, maritalStatus, birthDate, birthPlace, nationality, gender
 * - Media: avatarUrl, cvUrl
 * - CV metadata: cvSource, cvGeneratedAt (read-only dari sistem)
 * - CV preferences: cvPreferences (JSONB)
 * - Status: availableForWork
 * - Socials: JSONB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    // ============================================================
    // GET PRIVATE PROFILE
    // ============================================================
    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup. Silakan isi via API atau SQL."));
        return toDTO(profile);
    }

    // ============================================================
    // GET PUBLIC PROFILE
    // ============================================================
    @Transactional(readOnly = true)
    public ProfileDTO getPublicProfile(String portfolioSlug) {
        Profile profile = profileRepository.findByUserPortfolioSlug(portfolioSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile tidak ditemukan untuk user: " + portfolioSlug));
        return toDTO(profile);
    }

    // ============================================================
    // SAVE / UPDATE PROFILE
    // ============================================================
    @Transactional
    public ProfileDTO saveProfile(Long userId, ProfileDTO dto) {
        Profile profile = profileRepository.findByUserId(userId).orElse(null);

        if (profile == null) {
            // ===== CREATE BARU =====
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            profile = Profile.builder()
                    .user(user)
                    // ===== Identitas =====
                    .fullName(dto.getFullName())
                    .role(dto.getRole())
                    .bio(dto.getBio())
                    .shortBio(dto.getShortBio())
                    // ===== Kontak =====
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .city(dto.getCity())
                    .location(dto.getLocation())
                    // ⭐ Personal
                    .religion(dto.getReligion())
                    .maritalStatus(dto.getMaritalStatus())
                    .birthDate(dto.getBirthDate())
                    .birthPlace(dto.getBirthPlace())
                    .nationality(dto.getNationality())
                    .gender(dto.getGender())
                    // ===== Media =====
                    .avatarUrl(dto.getAvatarUrl())
                    .cvUrl(dto.getCvUrl())
                    // ===== CV preferences =====
                    .cvPreferences(dto.getCvPreferences())
                    // ===== Status =====
                    .availableForWork(dto.getAvailableForWork() != null
                            ? dto.getAvailableForWork() : true)
                    // ===== Socials =====
                    .socials(dto.getSocials())
                    .build();

            log.info("✅ Profile created for user {} (id={})",
                    user.getUsername(), userId);

        } else {
            // ===== UPDATE EXISTING =====
            // Identitas
            if (dto.getFullName() != null) profile.setFullName(dto.getFullName());
            if (dto.getRole() != null) profile.setRole(dto.getRole());
            if (dto.getBio() != null) profile.setBio(dto.getBio());
            if (dto.getShortBio() != null) profile.setShortBio(dto.getShortBio());

            // Kontak
            if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
            if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
            if (dto.getCity() != null) profile.setCity(dto.getCity());
            if (dto.getLocation() != null) profile.setLocation(dto.getLocation());

            // ⭐ Personal
            if (dto.getReligion() != null) profile.setReligion(dto.getReligion());
            if (dto.getMaritalStatus() != null) profile.setMaritalStatus(dto.getMaritalStatus());
            if (dto.getBirthDate() != null) profile.setBirthDate(dto.getBirthDate());
            if (dto.getBirthPlace() != null) profile.setBirthPlace(dto.getBirthPlace());
            if (dto.getNationality() != null) profile.setNationality(dto.getNationality());
            if (dto.getGender() != null) profile.setGender(dto.getGender());

            // Media
            if (dto.getAvatarUrl() != null) profile.setAvatarUrl(dto.getAvatarUrl());
            if (dto.getCvUrl() != null) profile.setCvUrl(dto.getCvUrl());

            // CV preferences — merge, bukan replace
            if (dto.getCvPreferences() != null) {
                profile.setCvPreferences(
                        mergeCvPreferences(profile.getCvPreferences(), dto.getCvPreferences()));
            }

            // Status
            if (dto.getAvailableForWork() != null)
                profile.setAvailableForWork(dto.getAvailableForWork());

            // Socials
            if (dto.getSocials() != null) profile.setSocials(dto.getSocials());

            log.info("✏️ Profile updated for user {}", userId);
        }

        Profile saved = profileRepository.save(profile);
        return toDTO(saved);
    }

    // ============================================================
    // ⭐ UPDATE CV PREFERENCES (partial / merge)
    // ============================================================
    /**
     * Update hanya cvPreferences — tanpa nyentuh field lain.
     *
     * @param prefs  — partial preferences
     * @param merge  — true = gabung dengan existing, false = replace total
     */
    @Transactional
    public ProfileDTO updateCvPreferences(Long userId, Map<String, Object> prefs, boolean merge) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup"));

        if (prefs == null) {
            // reset ke default
            profile.setCvPreferences(null);
            log.info("🔄 CV preferences RESET for user {}", userId);
        } else if (merge) {
            Map<String, Object> merged = mergeCvPreferences(profile.getCvPreferences(), prefs);
            profile.setCvPreferences(merged);
            log.info("🔀 CV preferences MERGED for user {} ({} keys)",
                    userId, merged.size());
        } else {
            profile.setCvPreferences(prefs);
            log.info("✏️ CV preferences REPLACED for user {} ({} keys)",
                    userId, prefs.size());
        }

        Profile saved = profileRepository.save(profile);
        return toDTO(saved);
    }

    // ============================================================
    // CV FILE CALLBACKS
    // ============================================================
    /**
     * Setelah upload CV sukses.
     */
    @Transactional
    public void updateCvAfterUpload(Long userId, String fileUrl) {
        profileRepository.findByUserId(userId).ifPresentOrElse(
                profile -> {
                    profile.setCvUrl(fileUrl);
                    profile.setCvSource(Profile.CvSource.UPLOAD);
                    profile.setCvGeneratedAt(null);
                    profileRepository.save(profile);
                    log.info("✅ Profile updated: cvSource=UPLOAD for user {}", userId);
                },
                () -> log.warn("⚠️ Profile not found for user {}", userId)
        );
    }

    /**
     * Clear cvUrl kalau match dengan file yang dihapus.
     */
    @Transactional
    public void clearCvIfMatch(Long userId, String expectedUrl) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            String currentUrl = profile.getCvUrl();
            if (expectedUrl.equals(currentUrl)) {
                profile.setCvUrl(null);
                profile.setCvSource(null);
                profile.setCvGeneratedAt(null);
                profileRepository.save(profile);
                log.info("✅ Profile cleared for user {}", userId);
            } else {
                log.info("ℹ️ Deleted file not active CV for user {}", userId);
            }
        });
    }

    // ============================================================
    // HELPER: Merge JSONB
    // ============================================================
    /**
     * Merge dua map JSONB — nilai baru menang, key lama yang nggak diupdate tetap.
     * Kalau value baru null, key dihapus dari hasil.
     */
    private Map<String, Object> mergeCvPreferences(
            Map<String, Object> oldPrefs, Map<String, Object> newPrefs) {
        Map<String, Object> result = new HashMap<>();
        if (oldPrefs != null) result.putAll(oldPrefs);
        if (newPrefs != null) {
            newPrefs.forEach((k, v) -> {
                if (v == null) result.remove(k);
                else result.put(k, v);
            });
        }
        return result;
    }

    // ============================================================
    // MAPPER: Entity → DTO
    // ============================================================
    private ProfileDTO toDTO(Profile p) {
        return ProfileDTO.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .role(p.getRole())
                .bio(p.getBio())
                .shortBio(p.getShortBio())
                .email(p.getEmail())
                .phone(p.getPhone())
                .city(p.getCity())
                .location(p.getLocation())
                .religion(p.getReligion())
                .maritalStatus(p.getMaritalStatus())
                .birthDate(p.getBirthDate())
                .birthPlace(p.getBirthPlace())
                .nationality(p.getNationality())
                .gender(p.getGender())
                .avatarUrl(p.getAvatarUrl())
                .cvUrl(p.getCvUrl())
                .cvSource(p.getCvSource() != null ? p.getCvSource().name() : null)
                .cvGeneratedAt(p.getCvGeneratedAt())
                .cvPreferences(p.getCvPreferences())
                .availableForWork(p.getAvailableForWork())
                .socials(p.getSocials())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}