package com.anjar.portfolio.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {

    private String message;
    private Long userId;
    private String username;
    private String portfolioSlug;
    private String status;   // PENDING_PAYMENT | PENDING | ACTIVE

    /**
     * ⭐ Info pembayaran (nullable).
     * Diisi kalau registrationPaymentEnabled = true.
     */
    private PaymentInitResponse payment;

    /**
     * Flag untuk frontend — butuh bayar atau enggak.
     */
    private Boolean requiresPayment;
}