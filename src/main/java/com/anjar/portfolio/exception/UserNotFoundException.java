package com.anjar.portfolio.exception;

/**
 * Exception untuk user tidak ditemukan.
 * Dilempar dari service → ditangkap GlobalExceptionHandler → return 404.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }

    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}