package com.anjar.portfolio.enums;

/**
 * Fase-fase yang dilalui saat restore database.
 */
public enum RestorePhase {
    /** Backup current DB sebelum restore (safety net) */
    BACKUP_CURRENT,

    /** Upload & validasi file dari user */
    VALIDATING,

    /** Restore dari file ke database */
    RESTORING,

    /** Verifikasi hasil restore */
    VERIFYING,

    /** Selesai sukses */
    DONE,

    /** Gagal */
    FAILED
}