package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.AuthResponse;
import com.anjar.portfolio.dto.LoginRequest;
import com.anjar.portfolio.dto.RegisterRequest;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.security.CustomUserDetailsService;
import com.anjar.portfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    // ===== REGISTER =====
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // Cek username & email udah ada
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email sudah dipakai");
        }

        // Bikin user baru
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("ADMIN")
                .active(true)
                .build();

        userRepository.save(user);

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    // ===== LOGIN =====
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        // Authenticate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getUsername(),
                        req.getPassword()
                )
        );

        // Kalau sukses, load user & generate token
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(user, token);
    }

    // ===== HELPER =====
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}