package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.AuthResponse;
import com.anjar.portfolio.dto.LoginRequest;
import com.anjar.portfolio.dto.RegisterRequest;
import com.anjar.portfolio.security.UserDetailsImpl;
import com.anjar.portfolio.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AuthController {

    private final AuthService authService;

    // ===== LOGIN =====
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // ===== REGISTER =====
    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(req));
    }

    // ===== ME (get current user dari JWT) =====
    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        Object principal = auth.getPrincipal();

        // Extract info dari UserDetailsImpl
        if (principal instanceof UserDetailsImpl user) {
            return ResponseEntity.ok(Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole(),
                    "authorities", auth.getAuthorities()
            ));
        }

        // Fallback (kalau principal bukan UserDetailsImpl)
        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        ));
    }
}