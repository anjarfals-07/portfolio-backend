package com.anjar.portfolio.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // 401 UNAUTHORIZED
    // ============================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex) {
        log.warn("🔒 Bad credentials: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(
            AuthenticationException ex) {
        log.warn("🔒 Authentication failed: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                ex.getMessage() != null ? ex.getMessage() : "Authentication required"
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(
            UsernameNotFoundException ex) {
        log.warn("🔒 Username not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage());
    }

    // ============================================================
    // 403 FORBIDDEN
    // ============================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {
        log.warn("🚫 Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Access denied");
    }

    @ExceptionHandler(AccountPendingException.class)
    public ResponseEntity<Map<String, Object>> handleAccountPending(
            AccountPendingException ex) {
        log.warn("⚠️ Account pending: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Account Pending", ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(
            ForbiddenException ex) {
        log.warn("⚠️ Forbidden: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    // ============================================================
    // 404 NOT FOUND
    // ============================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn("⚠️ Not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(
            PaymentNotFoundException ex) {
        log.warn("⚠️ Payment not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Payment Not Found", ex.getMessage());
    }


    // ⭐ TAMBAH INI — UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
            UserNotFoundException ex) {
        log.warn("⚠️ User not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
    }
    // ============================================================
    // 409 CONFLICT
    // ============================================================

    @ExceptionHandler(SlugAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleSlugExists(
            SlugAlreadyExistsException ex) {
        log.warn("⚠️ Slug exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    // ============================================================
    // 400 BAD REQUEST
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("⚠️ Validation error: {}", fieldErrors);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation Failed");
        body.put("message", "Ada field yang tidak valid");
        body.put("status", 400);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("fields", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        log.warn("⚠️ Illegal argument: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "";

        if (msg.toLowerCase().contains("authenticated")
                || msg.toLowerCase().contains("unauthorized")
                || msg.toLowerCase().contains("login")) {
            log.warn("🔒 Illegal state (auth): {}", msg);
            return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", msg);
        }

        log.error("❌ Illegal state: {}", msg, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", msg);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePayment(
            PaymentException ex) {
        log.warn("💳 Payment error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Payment Error", ex.getMessage());
    }

    // ============================================================
    // 413 PAYLOAD TOO LARGE
    // ============================================================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUpload(
            MaxUploadSizeExceededException ex) {
        log.error("❌ File too large: {}", ex.getMessage());
        return buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File Too Large",
                "File terlalu besar. Maksimal 500MB."
        );
    }

    // ============================================================
    // 500 INTERNAL SERVER ERROR
    // ============================================================

    /**
     * Handler untuk BackupException.
     * Return 500 Internal Server Error dengan pesan yang jelas.
     */
    @ExceptionHandler(BackupException.class)
    public ResponseEntity<Map<String, Object>> handleBackupException(
            BackupException ex) {
        log.error("❌ Backup error: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Backup Failed",
                ex.getMessage()
        );
    }

    /**
     * ⭐ Handler untuk RestoreException.
     * Return 500 Internal Server Error dengan pesan yang jelas.
     */
    @ExceptionHandler(RestoreException.class)
    public ResponseEntity<Map<String, Object>> handleRestoreException(
            RestoreException ex) {
        log.error("❌ Restore error: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Restore Failed",
                ex.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        log.error("❌ Internal error: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Terjadi kesalahan. Coba lagi nanti."
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex) {
        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Terjadi kesalahan. Coba lagi nanti."
        );
    }

    // ============================================================
    // HELPER
    // ============================================================

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", error);
        body.put("message", message);
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}