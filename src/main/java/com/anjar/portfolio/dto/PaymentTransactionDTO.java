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
public class PaymentTransactionDTO {

    private Long id;
    private Long userId;
    private String referenceId;
    private Long paymentMethodId;
    private String methodType;
    private String methodLabel;
    private BigDecimal amountIdr;
    private String status;

    // Proof
    private String proofImageUrl;
    private String proofNote;
    private LocalDateTime proofUploadedAt;
    private Integer proofUploadCount;

    // Verifikasi
    private String rejectionReason;
    private LocalDateTime verifiedAt;

    // Timestamps
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}