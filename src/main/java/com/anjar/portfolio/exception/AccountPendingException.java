package com.anjar.portfolio.exception;

public class AccountPendingException extends RuntimeException {

    /**
     * Constructor dengan custom message.
     */
    public AccountPendingException(String message) {
        super(message);
    }

    /**
     * Constructor dengan default message.
     */
    public AccountPendingException() {
        super("Akun kamu masih menunggu approval admin. Sabar ya!");
    }
}