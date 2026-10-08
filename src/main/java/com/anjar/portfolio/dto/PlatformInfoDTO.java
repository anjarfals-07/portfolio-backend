package com.anjar.portfolio.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Info platform yang aman di-expose ke public (tanpa auth).
 */
@Data
@Builder
public class PlatformInfoDTO {
    private String platformName;
    private String tagline;
    private String logoIcon;
    private String primaryColor;
    private String accentColor;

    private String defaultPortfolioUsername;
    private String primaryDomain;

    private Boolean registrationPaymentEnabled;
    private BigDecimal registrationFeeIdr;
    private Integer paymentExpiryMinutes;
}