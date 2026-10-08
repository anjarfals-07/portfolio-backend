package com.anjar.portfolio.dto;

import com.anjar.portfolio.enums.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    private Long id;
    private PaymentMethodType type;
    private String label;
    private Boolean isActive;
    private Integer sortOrder;

    // ===== QRIS =====
    private String qrisImageUrl;
    private String qrisMerchantName;

    // ===== BANK_TRANSFER =====
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;

    // ===== EWALLET =====
    private String ewalletProvider;
    private String ewalletAccount;
    private String ewalletAccountHolder;

    // ===== CRYPTO_EVM =====
    private String cryptoChain;
    private String cryptoToken;
    private String cryptoAddress;
    private String cryptoNetworkNote;

    // ===== Common =====
    private String instructions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}