package com.anjar.portfolio.enums;

public enum PaymentStatus {
    /** Transaksi dibuat, user belum upload bukti */
    PENDING,

    /** User sudah upload bukti, menunggu admin verifikasi */
    WAITING_VERIFICATION,

    /** Admin approve, payment valid */
    PAID,

    /** Admin reject bukti, user bisa upload ulang */
    REJECTED,

    /** Transaksi lewat batas waktu tanpa bukti */
    EXPIRED,

    /** Uang dikembalikan (manual oleh admin) */
    REFUNDED
}