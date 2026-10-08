package com.anjar.portfolio.service;

import com.anjar.portfolio.entity.PaymentTransaction;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.enums.PaymentStatus;
import com.anjar.portfolio.enums.UserPaymentStatus;
import com.anjar.portfolio.repository.PaymentTransactionRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentTransactionRepository txRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLog;

    // ============================================================
    // AUTO-EXPIRE PENDING PAYMENTS
    // Jalan setiap 5 menit
    // ============================================================
    @Scheduled(fixedDelay = 5 * 60 * 1000) // 5 menit
    @Transactional
    public void expirePendingPayments() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentTransaction> expired = txRepo.findExpiredPending(now);

        if (expired.isEmpty()) return;

        log.info("⏰ Found {} expired pending payments", expired.size());

        for (PaymentTransaction tx : expired) {
            try {
                tx.setStatus(PaymentStatus.EXPIRED);
                txRepo.save(tx);

                // Update user status kalau belum bayar
                User user = userRepo.findById(tx.getUserId()).orElse(null);
                if (user != null
                        && user.getPaymentStatus() == UserPaymentStatus.WAITING_VERIFICATION
                        && user.getStatus() == UserStatus.PENDING_PAYMENT) {
                    user.setPaymentStatus(UserPaymentStatus.EXPIRED);
                    userRepo.save(user);
                }

                activityLog.logPaymentExpired(tx.getId(), tx.getReferenceId());

                log.info("⏰ Payment expired: ref={}, user={}",
                        tx.getReferenceId(), tx.getUserId());
            } catch (Exception e) {
                log.error("❌ Failed to expire payment {}: {}",
                        tx.getId(), e.getMessage(), e);
            }
        }
    }

    // ============================================================
    // CLEANUP: HAPUS USER PENDING_PAYMENT EXPIRED > 7 HARI
    // Jalan setiap 24 jam
    // ============================================================
    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000) // 24 jam
    @Transactional
    public void cleanupExpiredUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        // Cari user PENDING_PAYMENT yang paymentStatus = EXPIRED
        // dan updatedAt-nya > 7 hari yang lalu
        List<User> candidates = userRepo.findByStatusAndPaymentStatusAndUpdatedAtBefore(
                UserStatus.PENDING_PAYMENT,
                UserPaymentStatus.EXPIRED,
                cutoff
        );

        if (candidates.isEmpty()) return;

        log.info("🧹 Cleaning up {} expired users", candidates.size());

        for (User user : candidates) {
            try {
                // Soft delete: set active = false + status REJECTED
                user.setActive(false);
                user.setStatus(UserStatus.REJECTED);
                user.setRejectionReason("Auto-cleanup: tidak menyelesaikan pembayaran dalam 7 hari");
                userRepo.save(user);

                log.info("🧹 User cleaned up: {}", user.getUsername());
            } catch (Exception e) {
                log.error("❌ Failed to cleanup user {}: {}",
                        user.getId(), e.getMessage());
            }
        }
    }
}