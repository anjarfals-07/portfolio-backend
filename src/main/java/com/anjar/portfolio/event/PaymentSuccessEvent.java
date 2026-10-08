package com.anjar.portfolio.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private final Long userId;
    private final String referenceId;
    private final String username;
    private final String email;
    private final String slug;
    private final String purpose;
}