package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    // ===== GET (single profile) =====
    @Transactional(readOnly = true)
    public ProfileDTO getProfile() {
        Profile profile = profileRepository.findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup. Silakan isi via API atau SQL."));
        return toDTO(profile);
    }

    // ===== CREATE / UPDATE (upsert) =====
    // Kalau profile belum ada → create
    // Kalau udah ada → update
    @Transactional
    public ProfileDTO saveProfile(ProfileDTO dto) {
        Profile profile = profileRepository.findFirst().orElse(null);

        if (profile == null) {
            profile = Profile.builder()
                    .fullName(dto.getFullName())
                    .role(dto.getRole())
                    .bio(dto.getBio())
                    .shortBio(dto.getShortBio())
                    .email(dto.getEmail())
                    .location(dto.getLocation())
                    .avatarUrl(dto.getAvatarUrl())
                    .cvUrl(dto.getCvUrl())
                    .availableForWork(dto.getAvailableForWork() != null ? dto.getAvailableForWork() : true)
                    .socials(dto.getSocials())
                    .build();
        } else {
            if (dto.getFullName() != null) profile.setFullName(dto.getFullName());
            if (dto.getRole() != null) profile.setRole(dto.getRole());
            if (dto.getBio() != null) profile.setBio(dto.getBio());
            if (dto.getShortBio() != null) profile.setShortBio(dto.getShortBio());
            if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
            if (dto.getLocation() != null) profile.setLocation(dto.getLocation());
            if (dto.getAvatarUrl() != null) profile.setAvatarUrl(dto.getAvatarUrl());
            if (dto.getCvUrl() != null) profile.setCvUrl(dto.getCvUrl());
            if (dto.getAvailableForWork() != null) profile.setAvailableForWork(dto.getAvailableForWork());
            if (dto.getSocials() != null) profile.setSocials(dto.getSocials());
        }

        return toDTO(profileRepository.save(profile));
    }

    // ===== Mapper =====
    private ProfileDTO toDTO(Profile p) {
        return ProfileDTO.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .role(p.getRole())
                .bio(p.getBio())
                .shortBio(p.getShortBio())
                .email(p.getEmail())
                .location(p.getLocation())
                .avatarUrl(p.getAvatarUrl())
                .cvUrl(p.getCvUrl())
                .availableForWork(p.getAvailableForWork())
                .socials(p.getSocials())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}