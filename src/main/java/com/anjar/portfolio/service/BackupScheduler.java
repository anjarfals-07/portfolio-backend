package com.anjar.portfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupScheduler {

    private final DatabaseBackupService backupService;

    @Value("${app.backup.enabled:true}")
    private boolean enabled;

    /**
     * Cleanup file backup orphan > N menit.
     * Jalan tiap 15 menit.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000)  // 15 menit
    public void cleanupOrphanFiles() {
        if (!enabled) return;

        try {
            int count = backupService.cleanupOrphanFiles();
            if (count > 0) {
                log.info("🧹 Cleanup selesai: {} file dihapus", count);
            }
        } catch (Exception e) {
            log.error("❌ Backup cleanup failed", e);
        }
    }
}