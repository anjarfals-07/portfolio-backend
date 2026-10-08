package com.anjar.portfolio.entity;

import com.anjar.portfolio.enums.PaymentMethodType;
import com.anjar.portfolio.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reference_id", nullable = false, unique = true, length = 50)
    private String referenceId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 30)
    private PaymentMethodType methodType;

    @Column(name = "method_label", nullable = false, length = 100)
    private String methodLabel;

    @Column(name = "amount_idr", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountIdr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    // ===== Bukti bayar =====
    @Column(name = "proof_image_url", length = 500)
    private String proofImageUrl;

    @Column(name = "proof_uploaded_at")
    private LocalDateTime proofUploadedAt;

    @Column(name = "proof_note", columnDefinition = "TEXT")
    private String proofNote;

    @Column(name = "proof_upload_count", nullable = false)
    @Builder.Default
    private Integer proofUploadCount = 0;

    // ===== Verifikasi admin =====
    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // ===== Timestamps =====
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // HELPER
    // ============================================================

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    public boolean canUploadProof() {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.REJECTED) {
            return false;
        }
        if (proofUploadCount != null && proofUploadCount >= 3) {
            return false;
        }
        return !isExpired();
    }

    public boolean isWaitingVerification() {
        return status == PaymentStatus.WAITING_VERIFICATION;
    }

    public boolean isPaid() {
        return status == PaymentStatus.PAID;
    }
}