package com.anjar.portfolio.enums;

/**
 * Status SSL cert per domain.
 */
public enum SSLStatus {

    /**
     * Belum diissue. Menunggu verifikasi DNS.
     */
    PENDING,

    /**
     * SSL sudah aktif (Let's Encrypt / Cloudflare).
     */
    ACTIVE,

    /**
     * Gagal issue SSL (rate limit, DNS salah, dll).
     */
    FAILED
}