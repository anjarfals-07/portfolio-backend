package com.anjar.portfolio.dto;

import com.anjar.portfolio.validator.ValidPortfolioSlug;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettingDTO {

    // ===== Branding =====
    @Size(max = 100)
    private String platformName;

    @Size(max = 100)
    private String logoIcon;

    @Size(max = 20)
    private String primaryColor;

    @Size(max = 20)
    private String accentColor;

    @Size(max = 200)
    private String tagline;

    // ===== Default user =====
    private String defaultRole;
    private Boolean autoApproveUsers;

    // ===== Domain =====
    @Size(max = 200)
    private String primaryDomain;

    @Size(max = 100)
    private String slugPattern;

    private Boolean allowCustomSlug;

    // ⭐ FIX: tidak pakai @NotReservedUsername karena di settings
    // admin boleh set default ke user existing (yang mungkin reserved)
    @ValidPortfolioSlug
    @Size(min = 3, max = 50)
    private String defaultPortfolioUsername;

    // ===== Email =====
    private Boolean emailNewUser;
    private Boolean emailNewMessage;
    private Boolean emailUserApproved;
    private Boolean emailUserRejected;

    @Size(max = 200)
    private String notificationEmail;

    // ===== Security =====
    @Min(6)
    private Integer minPasswordLength;

    private Boolean requireUppercase;
    private Boolean requireNumber;
    private Boolean requireSpecialChar;

    @Min(5)
    private Integer sessionTimeoutMinutes;

    @Min(1)
    private Integer maxLoginAttempts;

    // ===== Payment =====
    private Boolean registrationPaymentEnabled;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal registrationFeeIdr;

    @Min(1)
    private Integer paymentExpiryMinutes;

    private Boolean autoApproveAfterPayment;

    // ===== Meta =====
    private LocalDateTime updatedAt;
}