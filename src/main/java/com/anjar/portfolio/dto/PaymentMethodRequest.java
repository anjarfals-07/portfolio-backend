package com.anjar.portfolio.dto;

import com.anjar.portfolio.enums.PaymentMethodType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {

    @NotNull(message = "Type wajib diisi")
    private PaymentMethodType type;

    @NotBlank(message = "Label wajib diisi")
    @Size(max = 100)
    private String label;

    private Boolean isActive;

    private Integer sortOrder;

    // ===== QRIS =====
    @Size(max = 500)
    private String qrisImageUrl;

    @Size(max = 200)
    private String qrisMerchantName;

    // ===== BANK_TRANSFER =====
    @Size(max = 100)
    private String bankName;

    @Size(max = 50)
    private String bankAccountNumber;

    @Size(max = 200)
    private String bankAccountHolder;

    // ===== EWALLET =====
    @Size(max = 50)
    private String ewalletProvider;

    @Size(max = 100)
    private String ewalletAccount;

    @Size(max = 200)
    private String ewalletAccountHolder;

    // ===== CRYPTO_EVM =====
    @Size(max = 30)
    private String cryptoChain;

    @Size(max = 20)
    private String cryptoToken;

    @Size(max = 100)
    private String cryptoAddress;

    @Size(max = 200)
    private String cryptoNetworkNote;

    // ===== Common =====
    private String instructions;
}