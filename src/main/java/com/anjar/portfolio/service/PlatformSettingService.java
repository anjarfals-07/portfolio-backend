package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.PlatformSettingDTO;
import com.anjar.portfolio.entity.PlatformSetting;
import com.anjar.portfolio.repository.PlatformSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformSettingService {

    private static final Long SINGLETON_ID = 1L;

    private final PlatformSettingRepository repo;

    // ============================================================
    // GET
    // ============================================================
    @Transactional(readOnly = true)
    public PlatformSettingDTO getSettings() {
        PlatformSetting setting = repo.findById(SINGLETON_ID)
                .orElseGet(this::createDefault);
        return toDTO(setting);
    }

    // ============================================================
    // SAVE
    // ============================================================
    @Transactional
    public PlatformSettingDTO saveSettings(PlatformSettingDTO dto, Long updatedBy) {
        PlatformSetting setting = repo.findById(SINGLETON_ID)
                .orElseGet(() -> PlatformSetting.builder().id(SINGLETON_ID).build());

        // Branding
        if (dto.getPlatformName() != null) setting.setPlatformName(dto.getPlatformName());
        if (dto.getLogoIcon() != null) setting.setLogoIcon(dto.getLogoIcon());
        if (dto.getPrimaryColor() != null) setting.setPrimaryColor(dto.getPrimaryColor());
        if (dto.getAccentColor() != null) setting.setAccentColor(dto.getAccentColor());
        if (dto.getTagline() != null) setting.setTagline(dto.getTagline());

        // Default user
        if (dto.getDefaultRole() != null) setting.setDefaultRole(dto.getDefaultRole());
        if (dto.getAutoApproveUsers() != null) setting.setAutoApproveUsers(dto.getAutoApproveUsers());

        // Domain
        if (dto.getPrimaryDomain() != null) setting.setPrimaryDomain(dto.getPrimaryDomain());
        if (dto.getSlugPattern() != null) setting.setSlugPattern(dto.getSlugPattern());
        if (dto.getAllowCustomSlug() != null) setting.setAllowCustomSlug(dto.getAllowCustomSlug());
        if (dto.getDefaultPortfolioUsername() != null)
            setting.setDefaultPortfolioUsername(dto.getDefaultPortfolioUsername());

        // Email
        if (dto.getEmailNewUser() != null) setting.setEmailNewUser(dto.getEmailNewUser());
        if (dto.getEmailNewMessage() != null) setting.setEmailNewMessage(dto.getEmailNewMessage());
        if (dto.getEmailUserApproved() != null) setting.setEmailUserApproved(dto.getEmailUserApproved());
        if (dto.getEmailUserRejected() != null) setting.setEmailUserRejected(dto.getEmailUserRejected());
        if (dto.getNotificationEmail() != null) setting.setNotificationEmail(dto.getNotificationEmail());

        // Security
        if (dto.getMinPasswordLength() != null) setting.setMinPasswordLength(dto.getMinPasswordLength());
        if (dto.getRequireUppercase() != null) setting.setRequireUppercase(dto.getRequireUppercase());
        if (dto.getRequireNumber() != null) setting.setRequireNumber(dto.getRequireNumber());
        if (dto.getRequireSpecialChar() != null) setting.setRequireSpecialChar(dto.getRequireSpecialChar());
        if (dto.getSessionTimeoutMinutes() != null) setting.setSessionTimeoutMinutes(dto.getSessionTimeoutMinutes());
        if (dto.getMaxLoginAttempts() != null) setting.setMaxLoginAttempts(dto.getMaxLoginAttempts());

        // Payment
        if (dto.getRegistrationPaymentEnabled() != null)
            setting.setRegistrationPaymentEnabled(dto.getRegistrationPaymentEnabled());
        if (dto.getRegistrationFeeIdr() != null)
            setting.setRegistrationFeeIdr(dto.getRegistrationFeeIdr());
        if (dto.getPaymentExpiryMinutes() != null)
            setting.setPaymentExpiryMinutes(dto.getPaymentExpiryMinutes());
        if (dto.getAutoApproveAfterPayment() != null)
            setting.setAutoApproveAfterPayment(dto.getAutoApproveAfterPayment());

        setting.setUpdatedBy(updatedBy);

        PlatformSetting saved = repo.save(setting);
        log.info("✅ Platform settings updated by user {}", updatedBy);

        return toDTO(saved);
    }

    // ============================================================
    // CREATE DEFAULT
    // ============================================================
    private PlatformSetting createDefault() {
        return PlatformSetting.builder()
                .id(SINGLETON_ID)
                // ===== Branding =====
                .platformName("Nexus")
                .logoIcon("pi pi-bolt")
                .primaryColor("#3b82f6")
                .accentColor("#8b5cf6")
                .tagline("Portfolio Platform")
                // ===== Default user =====
                .defaultRole("OWNER")
                .autoApproveUsers(false)
                // ===== Domain =====
                .primaryDomain("portfolio.com")
                .slugPattern("portfolio.com/{slug}")
                .allowCustomSlug(true)
                .defaultPortfolioUsername("muhammad-anjar")
                // ===== Email =====
                .emailNewUser(true)
                .emailNewMessage(true)
                .emailUserApproved(true)
                .emailUserRejected(true)
                .notificationEmail("admin@portfolio.com")
                // ===== Security =====
                .minPasswordLength(8)
                .requireUppercase(false)
                .requireNumber(true)
                .requireSpecialChar(false)
                .sessionTimeoutMinutes(60)
                .maxLoginAttempts(5)
                // ===== Payment =====
                .registrationPaymentEnabled(false)
                .registrationFeeIdr(new BigDecimal("50000"))
                .paymentExpiryMinutes(1440)              // 24 jam
                .autoApproveAfterPayment(true)
                .build();
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private PlatformSettingDTO toDTO(PlatformSetting s) {
        return PlatformSettingDTO.builder()
                // ===== Branding =====
                .platformName(s.getPlatformName())
                .logoIcon(s.getLogoIcon())
                .primaryColor(s.getPrimaryColor())
                .accentColor(s.getAccentColor())
                .tagline(s.getTagline())
                // ===== Default user =====
                .defaultRole(s.getDefaultRole())
                .autoApproveUsers(s.getAutoApproveUsers())
                // ===== Domain =====
                .primaryDomain(s.getPrimaryDomain())
                .slugPattern(s.getSlugPattern())
                .allowCustomSlug(s.getAllowCustomSlug())
                .defaultPortfolioUsername(s.getDefaultPortfolioUsername())
                // ===== Email =====
                .emailNewUser(s.getEmailNewUser())
                .emailNewMessage(s.getEmailNewMessage())
                .emailUserApproved(s.getEmailUserApproved())
                .emailUserRejected(s.getEmailUserRejected())
                .notificationEmail(s.getNotificationEmail())
                // ===== Security =====
                .minPasswordLength(s.getMinPasswordLength())
                .requireUppercase(s.getRequireUppercase())
                .requireNumber(s.getRequireNumber())
                .requireSpecialChar(s.getRequireSpecialChar())
                .sessionTimeoutMinutes(s.getSessionTimeoutMinutes())
                .maxLoginAttempts(s.getMaxLoginAttempts())
                // ===== Payment =====
                .registrationPaymentEnabled(s.getRegistrationPaymentEnabled())
                .registrationFeeIdr(s.getRegistrationFeeIdr())
                .paymentExpiryMinutes(s.getPaymentExpiryMinutes())
                .autoApproveAfterPayment(s.getAutoApproveAfterPayment())
                // ===== Meta =====
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}