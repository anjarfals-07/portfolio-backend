package com.anjar.portfolio.enums;

public enum UserStatus {
    /** Baru register, belum bayar (kalau payment enabled) */
    PENDING_PAYMENT,

    /** Sudah bayar / payment disabled, menunggu approval admin */
    PENDING,

    /** Aktif, bisa login */
    ACTIVE,

    /** Ditolak admin */
    REJECTED,

    /** Di-suspend */
    SUSPENDED
}