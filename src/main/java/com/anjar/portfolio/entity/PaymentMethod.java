package com.anjar.portfolio.entity;

import com.anjar.portfolio.enums.PaymentMethodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_methods")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethodType type;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    // ===== QRIS =====
    @Column(name = "qris_image_url", length = 500)
    private String qrisImageUrl;

    @Column(name = "qris_merchant_name", length = 200)
    private String qrisMerchantName;

    // ===== BANK_TRANSFER =====
    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_holder", length = 200)
    private String bankAccountHolder;

    // ===== EWALLET =====
    @Column(name = "ewallet_provider", length = 50)
    private String ewalletProvider;

    @Column(name = "ewallet_account", length = 100)
    private String ewalletAccount;

    @Column(name = "ewallet_account_holder", length = 200)
    private String ewalletAccountHolder;

    // ===== CRYPTO_EVM =====
    @Column(name = "crypto_chain", length = 30)
    private String cryptoChain;

    @Column(name = "crypto_token", length = 20)
    private String cryptoToken;

    @Column(name = "crypto_address", length = 100)
    private String cryptoAddress;

    @Column(name = "crypto_network_note", length = 200)
    private String cryptoNetworkNote;

    // ===== Common =====
    @Column(columnDefinition = "TEXT")
    private String instructions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================
    // HELPER
    // ============================================================

    public boolean isQris() {
        return type == PaymentMethodType.QRIS;
    }

    public boolean isBankTransfer() {
        return type == PaymentMethodType.BANK_TRANSFER;
    }

    public boolean isCryptoEvm() {
        return type == PaymentMethodType.CRYPTO_EVM;
    }

    public boolean isEwallet() {
        return type == PaymentMethodType.EWALLET;
    }
}