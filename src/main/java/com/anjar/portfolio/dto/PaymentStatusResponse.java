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
public class PaymentStatusResponse {

    private String referenceId;
    private String status;
    private String methodLabel;
    private BigDecimal amountIdr;
    private String proofImageUrl;
    private LocalDateTime proofUploadedAt;
    private String rejectionReason;
    private LocalDateTime expiredAt;
    private LocalDateTime paidAt;
}