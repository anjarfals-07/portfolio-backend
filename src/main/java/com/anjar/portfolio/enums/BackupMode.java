package com.anjar.portfolio.enums;

/**
 * Mode eksekusi backup database.
 *
 * - DOCKER : pakai `docker exec pg_dump` → cocok untuk dev dengan Docker
 * - LOCAL  : pakai binary `pg_dump` di PATH → cocok untuk production
 */
public enum BackupMode {
    DOCKER,
    LOCAL
}