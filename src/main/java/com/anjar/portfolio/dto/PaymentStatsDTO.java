package com.anjar.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDTO {

    private long totalPending;
    private long totalPaid;
    private long totalRejected;
    private long totalExpired;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueTotal;
}