package com.anjar.portfolio.entity;

import com.anjar.portfolio.enums.UserPaymentStatus;
import com.anjar.portfolio.enums.UserRole;
import com.anjar.portfolio.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_portfolio_slug", columnNames = "portfolio_slug")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // AUTH
    // ============================================================

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    // ============================================================
    // MULTI-TENANT
    // ============================================================

    @Column(name = "portfolio_slug", nullable = false, unique = true, length = 50)
    private String portfolioSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.OWNER;

    // ============================================================
    // STATUS (Fase 6 — Approval)
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    // ============================================================
    // RESET PASSWORD (Fase 9.17)
    // ============================================================

    @Column(name = "reset_token", length = 100)
    private String resetToken;

    @Column(name = "reset_token_expires")
    private LocalDateTime resetTokenExpires;

    // ============================================================
    // PROFILE INFO
    // ============================================================

    @Column(nullable = false, length = 200)
    private String email;

    @Column(name = "display_name", length = 100)
    private String displayName;

    // ============================================================
    // FLAG AKTIF
    // ============================================================

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ============================================================
    // PAYMENT (BARU)
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    @Builder.Default
    private UserPaymentStatus paymentStatus = UserPaymentStatus.UNPAID;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // ============================================================
    // THEME (Fase 7.2)
    // ============================================================

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Theme theme;

    // ============================================================
    // TIMESTAMPS
    // ============================================================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // HELPER — ROLE & STATUS
    // ============================================================

    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }

    public boolean isOwner() {
        return this.role == UserRole.OWNER;
    }

    public boolean isPending() {
        return this.status == UserStatus.PENDING;
    }

    public boolean hasActiveStatus() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isRejected() {
        return this.status == UserStatus.REJECTED;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.SUSPENDED;
    }

    public boolean canLogin() {
        return Boolean.TRUE.equals(this.active)
                && this.status == UserStatus.ACTIVE;
    }

    public boolean isResetTokenValid() {
        return resetToken != null
                && resetTokenExpires != null
                && resetTokenExpires.isAfter(LocalDateTime.now());
    }

    public boolean hasTheme() {
        return this.theme != null;
    }

    // ============================================================
    // HELPER — PAYMENT
    // ============================================================

    public boolean hasPaid() {
        return this.paymentStatus == UserPaymentStatus.PAID;
    }

    public boolean isWaitingPayment() {
        return this.paymentStatus == UserPaymentStatus.UNPAID
                || this.paymentStatus == UserPaymentStatus.WAITING_VERIFICATION
                || this.paymentStatus == UserPaymentStatus.REJECTED;
    }

    public boolean isPaymentRejected() {
        return this.paymentStatus == UserPaymentStatus.REJECTED;
    }

    public boolean isPaymentExpired() {
        return this.paymentStatus == UserPaymentStatus.EXPIRED;
    }
}