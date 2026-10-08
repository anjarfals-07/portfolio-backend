package com.anjar.portfolio.listener;

import com.anjar.portfolio.event.PaymentRejectedEvent;
import com.anjar.portfolio.event.PaymentSuccessEvent;
import com.anjar.portfolio.service.EmailService;
import com.anjar.portfolio.service.PlatformSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEmailListener {

    private final EmailService emailService;
    private final PlatformSettingService settingService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    // ============================================================
    // PAYMENT SUCCESS → email ke user
    // ============================================================
    @Async
    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        try {
            String nextStepUrl = frontendUrl + "/login";
            emailService.sendPaymentApprovedEmail(
                    event.getEmail(),
                    event.getUsername(),
                    event.getReferenceId(),
                    nextStepUrl
            );
            log.info("📧 Payment approved email sent for: {}",
                    event.getReferenceId());
        } catch (Exception e) {
            log.error("❌ Failed to handle PaymentSuccessEvent: {}",
                    e.getMessage(), e);
        }
    }

    // ============================================================
    // PAYMENT REJECTED → email ke user
    // ============================================================
    @Async
    @EventListener
    public void onPaymentRejected(PaymentRejectedEvent event) {
        try {
            String retryUrl = frontendUrl + "/payment-rejected";
            emailService.sendPaymentRejectedEmail(
                    event.getEmail(),
                    event.getUsername(),
                    event.getReferenceId(),
                    event.getRejectionReason(),
                    retryUrl
            );
            log.info("📧 Payment rejected email sent for: {}",
                    event.getReferenceId());
        } catch (Exception e) {
            log.error("❌ Failed to handle PaymentRejectedEvent: {}",
                    e.getMessage(), e);
        }
    }
}