package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.security.CurrentUser;
import com.anjar.portfolio.security.UserDetailsImpl;
import com.anjar.portfolio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ===== LOGIN =====
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // ===== REGISTER =====
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(req));
    }

    // ============================================================
    // ⭐ ME — verify token
    // ============================================================
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {

        // 1. Cek Authentication ada
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("⚠️ /me called without authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Not authenticated"
                    ));
        }

        // 2. Extract userId — support MULTIPLE principal types
        Long userId = null;
        Object principal = auth.getPrincipal();

        if (principal instanceof CurrentUser cu) {
            userId = cu.getId();
        } else if (principal instanceof UserDetailsImpl impl) {
            userId = impl.getId();
        } else {
            log.warn("⚠️ Unknown principal type: {}",
                    principal != null ? principal.getClass().getName() : "null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "Invalid principal type"
                    ));
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "User ID not found"
                    ));
        }

        // 3. Load user dari DB — verify masih valid
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("⚠️ User not found in DB: id={}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Unauthorized",
                            "message", "User not found"
                    ));
        }

        // 4. Cek status user
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("⚠️ /me by non-ACTIVE user: {} (status={})",
                    user.getUsername(), user.getStatus());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "Forbidden",
                            "message", "Account is not active"
                    ));
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            log.warn("⚠️ /me by disabled user: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "Forbidden",
                            "message", "Account is disabled"
                    ));
        }

        // 5. Return data lengkap
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("portfolioSlug", user.getPortfolioSlug());
        response.put("displayName", user.getDisplayName());

        log.debug("✅ /me OK for user: {} (id={}, role={})",
                user.getUsername(), user.getId(), user.getRole());

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // FORGOT PASSWORD
    // ============================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Link reset password udah dikirim ke email kamu."
        ));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<VerifyTokenResponse> verifyResetToken(
            @RequestParam String token) {
        return ResponseEntity.ok(authService.verifyResetToken(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Password berhasil di-reset. Silakan login."
        ));
    }
}