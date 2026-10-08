package com.anjar.portfolio.dto;

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
public class PaymentAdminDTO {

    private Long id;
    private String referenceId;

    // User
    private Long userId;
    private String username;
    private String email;
    private String displayName;
    private String portfolioSlug;
    private String userStatus;
    private String userPaymentStatus;

    // Method
    private Long paymentMethodId;
    private String methodType;
    private String methodLabel;

    // Snapshot method
    private String qrisImageUrl;
    private String qrisMerchantName;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
    // EWALLET
    private String ewalletProvider;
    private String ewalletAccount;
    private String ewalletAccountHolder;
    private String cryptoChain;
    private String cryptoToken;
    private String cryptoAddress;
    private String cryptoNetworkNote;

    // Amount
    private BigDecimal amountIdr;
    private String status;

    // Proof
    private String proofImageUrl;
    private String proofNote;
    private LocalDateTime proofUploadedAt;
    private Integer proofUploadCount;

    // Verifikasi
    private Long verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String rejectionReason;

    // Timestamps
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}