package com.anjar.portfolio.exception;

/**
 * Exception untuk request yang tidak valid (HTTP 400).
 *
 * Dipakai di service layer untuk validation:
 * - Format domain tidak valid
 * - Domain duplikat
 * - Limit tercapai
 * - Domain belum terverifikasi
 * - dll
 *
 * Ditangkap oleh GlobalExceptionHandler → return 400 Bad Request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}