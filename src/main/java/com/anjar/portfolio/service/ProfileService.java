package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ProfileDTO;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ProfileRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileDTO getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup. Silakan isi via API atau SQL."));
        return toDTO(profile);
    }

    @Transactional(readOnly = true)
    public ProfileDTO getPublicProfile(String portfolioSlug) {
        Profile profile = profileRepository.findByUserPortfolioSlug(portfolioSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile tidak ditemukan untuk user: " + portfolioSlug));
        return toDTO(profile);
    }

    @Transactional
    public ProfileDTO saveProfile(Long userId, ProfileDTO dto) {
        Profile profile = profileRepository.findByUserId(userId).orElse(null);

        if (profile == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));

            profile = Profile.builder()
                    .user(user)
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