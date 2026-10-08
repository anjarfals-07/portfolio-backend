package com.anjar.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSetting {

    @Id
    private Long id;

    // ============================================================
    // BRANDING
    // ============================================================
    @Column(name = "platform_name", length = 100)
    private String platformName;

    @Column(name = "logo_icon", length = 100)
    private String logoIcon;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "accent_color", length = 20)
    private String accentColor;

    @Column(name = "tagline", length = 200)
    private String tagline;

    // ============================================================
    // DEFAULT USER
    // ============================================================
    @Column(name = "default_role", length = 20)
    private String defaultRole;

    @Column(name = "auto_approve_users")
    private Boolean autoApproveUsers;

    // ============================================================
    // DOMAIN
    // ============================================================
    @Column(name = "primary_domain", length = 200)
    private String primaryDomain;

    @Column(name = "slug_pattern", length = 100)
    private String slugPattern;

    @Column(name = "allow_custom_slug")
    private Boolean allowCustomSlug;

    @Column(name = "default_portfolio_username", length = 50)
    private String defaultPortfolioUsername;

    // ============================================================
    // EMAIL
    // ============================================================
    @Column(name = "email_new_user")
    private Boolean emailNewUser;

    @Column(name = "email_new_message")
    private Boolean emailNewMessage;

    @Column(name = "email_user_approved")
    private Boolean emailUserApproved;

    @Column(name = "email_user_rejected")
    private Boolean emailUserRejected;

    @Column(name = "notification_email", length = 200)
    private String notificationEmail;

    // ============================================================
    // SECURITY
    // ============================================================
    @Column(name = "min_password_length")
    private Integer minPasswordLength;

    @Column(name = "require_uppercase")
    private Boolean requireUppercase;

    @Column(name = "require_number")
    private Boolean requireNumber;

    @Column(name = "require_special_char")
    private Boolean requireSpecialChar;

    @Column(name = "session_timeout_minutes")
    private Integer sessionTimeoutMinutes;

    @Column(name = "max_login_attempts")
    private Integer maxLoginAttempts;

    // ============================================================
    // PAYMENT
    // ============================================================
    @Column(name = "registration_payment_enabled")
    private Boolean registrationPaymentEnabled;

    @Column(name = "registration_fee_idr", precision = 15, scale = 2)
    private BigDecimal registrationFeeIdr;

    @Column(name = "payment_expiry_minutes")
    private Integer paymentExpiryMinutes;

    @Column(name = "auto_approve_after_payment")
    private Boolean autoApproveAfterPayment;

    // ============================================================
    // TIMESTAMPS
    // ============================================================
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    // ============================================================
    // HELPER — PAYMENT
    // ============================================================

    public boolean isPaymentEnabled() {
        return Boolean.TRUE.equals(registrationPaymentEnabled);
    }

    public int getPaymentExpiryMinutesOrDefault() {
        return paymentExpiryMinutes != null ? paymentExpiryMinutes : 1440;
    }

    public boolean isAutoApproveAfterPayment() {
        return Boolean.TRUE.equals(autoApproveAfterPayment);
    }

    public BigDecimal getRegistrationFeeIdrOrDefault() {
        return registrationFeeIdr != null
                ? registrationFeeIdr
                : new BigDecimal("50000");
    }
}