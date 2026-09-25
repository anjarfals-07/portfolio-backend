package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.AuthResponse;
import com.anjar.portfolio.dto.LoginRequest;
import com.anjar.portfolio.dto.RegisterRequest;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.entity.UserRole;
import com.anjar.portfolio.exception.SlugAlreadyExistsException;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SlugService slugService;

    // ===== LOGIN =====
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            String token = jwtUtil.generateToken(user);

            log.info("✅ User logged in: {} (id={}, role={})",
                    user.getUsername(), user.getId(), user.getRole());

            return buildAuthResponse(user, token);

        } catch (BadCredentialsException e) {
            log.warn("❌ Login failed for user: {}", request.getUsername());
            throw new BadCredentialsException("Username atau password salah");
        }
    }

    // ===== REGISTER =====
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Cek username unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }

        // 2. Cek email unique (kalau kamu punya index unique di email)
        // Optional — tergantung apakah kamu bikin email unique

        // 3. Generate atau validate slug
        String slug;
        if (request.getPortfolioSlug() != null && !request.getPortfolioSlug().isBlank()) {
            // User kasih slug custom → validate
            slugService.validateSlugForNewUser(request.getPortfolioSlug());
            slug = request.getPortfolioSlug();
        } else {
            // Auto-generate dari username atau displayName
            String base = request.getDisplayName() != null && !request.getDisplayName().isBlank()
                    ? request.getDisplayName()
                    : request.getUsername();
            slug = slugService.generateUniqueSlug(base, true);
        }

        // 4. Bikin user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .portfolioSlug(slug)
                .displayName(
                        request.getDisplayName() != null && !request.getDisplayName().isBlank()
                                ? request.getDisplayName()
                                : request.getUsername()
                )
                .role(UserRole.OWNER)  // Default role: OWNER
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("✅ New user registered: {} (slug={}, id={})",
                saved.getUsername(), saved.getPortfolioSlug(), saved.getId());

        // 5. Auto-login (generate JWT)
        String token = jwtUtil.generateToken(saved);

        return buildAuthResponse(saved, token);
    }

    // ===== HELPER =====
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .portfolioSlug(user.getPortfolioSlug())
                .displayName(user.getDisplayName())
                .expiresIn(jwtUtil.getExpirationMs())
                .build();
    }
}