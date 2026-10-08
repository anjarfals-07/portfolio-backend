package com.anjar.portfolio.enums;

public enum UserPaymentStatus {
    /** Belum bayar / belum ada transaksi */
    UNPAID,

    /** Ada transaksi aktif, menunggu bukti atau verifikasi */
    WAITING_VERIFICATION,

    /** Payment sudah di-approve admin */
    PAID,

    /** Bukti ditolak admin */
    REJECTED,

    /** Transaksi expired */
    EXPIRED,

    /** Uang dikembalikan */
    REFUNDED
}