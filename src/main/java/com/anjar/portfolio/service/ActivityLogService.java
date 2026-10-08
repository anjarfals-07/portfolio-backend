package com.anjar.portfolio.service;

import com.anjar.portfolio.entity.ActivityLog;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.repository.ActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repo;
    private final ObjectMapper objectMapper;

    // ============================================================
    // LOG — generic
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            User actor,
            String action,
            String entityType,
            Long entityId,
            String description,
            Map<String, Object> metadata
    ) {
        try {
            String metadataJson = null;
            if (metadata != null) {
                metadataJson = objectMapper.writeValueAsString(metadata);
            }

            ActivityLog entry = ActivityLog.builder()
                    .userId(actor != null ? actor.getId() : null)
                    .username(actor != null ? actor.getUsername() : "system")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .metadata(metadataJson)
                    .build();

            repo.save(entry);
            log.debug("📝 Activity logged: {} - {}", action, description);

        } catch (Exception e) {
            log.error("❌ Failed to log activity: {}", action, e);
        }
    }

    // ============================================================
    // PAYMENT
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentCreated(User user, Long txId, String referenceId) {
        log(user, "PAYMENT_CREATED", "PAYMENT", txId,
                "Payment dibuat: " + referenceId, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentApproved(User admin, Long txId, String referenceId) {
        log(admin, "PAYMENT_APPROVED", "PAYMENT", txId,
                "Payment di-approve: " + referenceId, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentRejected(User admin, Long txId, String referenceId,
                                   String reason) {
        log(admin, "PAYMENT_REJECTED", "PAYMENT", txId,
                "Payment di-reject: " + referenceId + " - " + reason, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentExpired(Long txId, String referenceId) {
        log(null, "PAYMENT_EXPIRED", "PAYMENT", txId,
                "Payment expired: " + referenceId, null);
    }

    // ============================================================
    // USER
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserRegistered(User user) {
        log(user, "USER_REGISTERED", "USER", user.getId(),
                "User baru register: " + user.getUsername(), null);
    }

    // ============================================================
    // PAYMENT METHOD
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentMethodCreated(User admin, Long methodId,
                                        String label) {
        log(admin, "PAYMENT_METHOD_CREATED", "PAYMENT_METHOD", methodId,
                "Payment method dibuat: " + label, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentMethodUpdated(User admin, Long methodId,
                                        String label) {
        log(admin, "PAYMENT_METHOD_UPDATED", "PAYMENT_METHOD", methodId,
                "Payment method diupdate: " + label, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentMethodDeleted(User admin, Long methodId,
                                        String label) {
        log(admin, "PAYMENT_METHOD_DELETED", "PAYMENT_METHOD", methodId,
                "Payment method dihapus: " + label, null);
    }

    // ============================================================
    // ⭐ BACKUP DATABASE
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBackupCreated(User admin, String filename, long sizeBytes) {
        log(admin, "BACKUP_CREATED", "BACKUP", null,
                "Backup DB dibuat: " + filename,
                Map.of("filename", filename, "sizeBytes", sizeBytes));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBackupDownloaded(User admin, String filename) {
        log(admin, "BACKUP_DOWNLOADED", "BACKUP", null,
                "Backup DB didownload: " + filename,
                Map.of("filename", filename));
    }

    // ============================================================
    // ⭐ RESTORE DATABASE
    // ============================================================
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRestoreStarted(User admin, String filename, long sizeBytes) {
        log(admin, "RESTORE_STARTED", "BACKUP", null,
                "Restore DB dimulai dari file: " + filename
                        + " (" + (sizeBytes / 1024) + " KB)",
                Map.of("filename", filename, "sizeBytes", sizeBytes));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRestoreCompleted(
            User admin,
            String filename,
            long durationMs,
            int tablesRestored,
            long userCount
    ) {
        log(admin, "RESTORE_COMPLETED", "BACKUP", null,
                "Restore DB selesai: " + filename
                        + " (" + durationMs + "ms, "
                        + tablesRestored + " tables, "
                        + userCount + " users)",
                Map.of(
                        "filename", filename,
                        "durationMs", durationMs,
                        "tablesRestored", tablesRestored,
                        "userCount", userCount
                ));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRestoreFailed(User admin, String filename, String errorMessage) {
        log(admin, "RESTORE_FAILED", "BACKUP", null,
                "Restore DB gagal: " + filename + " — " + errorMessage,
                Map.of("filename", filename, "error", errorMessage));
    }
}