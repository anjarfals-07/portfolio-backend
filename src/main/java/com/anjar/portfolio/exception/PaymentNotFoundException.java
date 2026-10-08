package com.anjar.portfolio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception kalau transaksi payment gak ditemukan.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String referenceId) {
        super("Transaksi tidak ditemukan: " + referenceId);
    }

    public PaymentNotFoundException(String message, boolean custom) {
        super(message);
    }
}