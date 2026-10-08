package com.anjar.portfolio.listener;

import com.anjar.portfolio.event.UserRegisteredEvent;
import com.anjar.portfolio.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {

        // ⭐ Kalau butuh payment, SKIP welcome email.
        // Email baru dikirim setelah payment sukses.
        if (event.isRequiresPayment()) {
            log.info("💳 Skipping welcome email for {} — waiting for payment",
                    event.getUsername());
            return;
        }

        log.info("📧 Sending welcome emails for user: {}", event.getUsername());

        try {
            emailService.sendWelcomeEmail(
                    event.getEmail(),
                    event.getUsername(),
                    event.getSlug()
            );
            emailService.sendNewUserNotifToAdmin(
                    event.getUsername(),
                    event.getEmail(),
                    event.getSlug()
            );
        } catch (Exception e) {
            log.error("❌ Failed to send welcome emails for {}: {}",
                    event.getUsername(), e.getMessage());
        }
    }
}