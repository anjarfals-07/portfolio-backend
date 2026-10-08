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
public class PaymentInitResponse {

    private String referenceId;
    private BigDecimal amountIdr;
    private String provider;      // MANUAL
    private String method;        // QRIS | BANK_TRANSFER | CRYPTO_EVM
    private String methodLabel;   // "BCA", "USDT Polygon", dll
    private String status;        // PENDING | WAITING_VERIFICATION | ...
    private LocalDateTime expiredAt;

    // QRIS
    private String qrisImageUrl;
    private String qrisMerchantName;

    // BANK_TRANSFER
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;

    // EWALLET
    private String ewalletProvider;
    private String ewalletAccount;
    private String ewalletAccountHolder;

    // CRYPTO_EVM
    private String cryptoChain;
    private String cryptoToken;
    private String cryptoAddress;
    private String cryptoNetworkNote;

    // Common
    private String instructions;
}