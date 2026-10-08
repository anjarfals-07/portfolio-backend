package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserRole;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.enums.UserPaymentStatus;
import com.anjar.portfolio.event.UserRegisteredEvent;
import com.anjar.portfolio.exception.AccountPendingException;
import com.anjar.portfolio.repository.ProfileRepository;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SlugService slugService;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformSettingService settingService;
    private final PaymentService paymentService;   // ⭐ BARU

    // ============================================================
    // LOGIN
    // ============================================================
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException(
                        "Username atau password salah"
                ));

        switch (user.getStatus()) {
            case PENDING_PAYMENT -> throw new AccountPendingException(
                    "Akun kamu belum aktif. Selesaikan pembayaran registrasi dulu."
            );
            case PENDING -> throw new AccountPendingException(
                    "Akun kamu masih menunggu approval admin. Sabar ya!"
            );
            case REJECTED -> {
                String reason = user.getRejectionReason();
                String msg = "Akun kamu ditolak oleh admin.";
                if (reason != null && !reason.isBlank()) {
                    msg += " Alasan: " + reason;
                }
                throw new BadCredentialsException(msg);
            }
            case SUSPENDED -> throw new BadCredentialsException(
                    "Akun kamu di-suspend. Hubungi admin untuk info lebih lanjut."
            );
            case ACTIVE -> {}
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BadCredentialsException(
                    "Akun kamu di-disable. Hubungi admin."
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("❌ Login failed for user: {}", request.getUsername());
            throw new BadCredentialsException("Username atau password salah");
        }

        String token = jwtUtil.generateToken(user);

        log.info("✅ User logged in: {} (id={}, role={}, status={})",
                user.getUsername(), user.getId(), user.getRole(), user.getStatus());

        return buildAuthResponse(user, token);
    }

    // ============================================================
    // REGISTER
    // ============================================================
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Cek unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        // 2. Ambil platform settings
        PlatformSettingDTO settings = settingService.getSettings();
        boolean paymentEnabled = Boolean.TRUE.equals(
                settings.getRegistrationPaymentEnabled()
        );

        log.info("📋 Register: paymentEnabled={}, fee={}",
                paymentEnabled, settings.getRegistrationFeeIdr());

        // 3. Generate/validate slug
        String slug;
        if (request.getPortfolioSlug() != null && !request.getPortfolioSlug().isBlank()) {
            slugService.validateSlugForNewUser(request.getPortfolioSlug());
            slug = request.getPortfolioSlug();
        } else {
            String base = request.getDisplayName() != null && !request.getDisplayName().isBlank()
                    ? request.getDisplayName()
                    : request.getUsername();
            slug = slugService.generateUniqueSlug(base, true);
        }

        // 4. Tentukan status awal
        UserStatus initialStatus = paymentEnabled
                ? UserStatus.PENDING_PAYMENT
                : UserStatus.PENDING;

        // 5. Tentukan paymentStatus user
        UserPaymentStatus initialPaymentStatus = paymentEnabled
                ? UserPaymentStatus.WAITING_VERIFICATION   // ⭐ user akan upload bukti
                : UserPaymentStatus.UNPAID;

        // 6. Buat user
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
                .role(UserRole.OWNER)
                .status(initialStatus)
                .paymentStatus(initialPaymentStatus)   // ⭐ SET
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("✅ New user registered ({}): {} (slug={}, id={})",
                initialStatus, saved.getUsername(), saved.getPortfolioSlug(), saved.getId());

        // 7. Auto-create profile
        Profile profile = Profile.builder()
                .user(saved)
                .fullName(saved.getDisplayName())
                .email(saved.getEmail())
                .availableForWork(true)
                .build();
        profileRepository.save(profile);

        log.info("✅ Profile created for user: {}", saved.getUsername());

        // ============================================================
        // 8. HANDLE PAYMENT (kalau enabled)
        // ============================================================
        PaymentInitResponse paymentInfo = null;

        if (paymentEnabled) {
            try {
                BigDecimal amount = settings.getRegistrationFeeIdr();
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.error("❌ Payment enabled but fee invalid: {}", amount);
                    throw new IllegalStateException(
                            "Biaya registrasi belum dikonfigurasi oleh admin"
                    );
                }

                Integer expiryMinutes = settings.getPaymentExpiryMinutes();
                int expiry = (expiryMinutes != null && expiryMinutes > 0)
                        ? expiryMinutes
                        : 1440; // default 24 jam

                // ⭐ Validasi paymentMethodId
                if (request.getPaymentMethodId() == null) {
                    throw new IllegalArgumentException(
                            "Pilih metode pembayaran terlebih dahulu"
                    );
                }

                paymentInfo = paymentService.createPayment(
                        saved.getId(),
                        amount,
                        "REGISTRATION",
                        request.getPaymentMethodId(),   // ⭐ pakai method ID
                        expiry
                );

                log.info("✅ Payment created for user {}: ref={}",
                        saved.getUsername(), paymentInfo.getReferenceId());

            } catch (Exception e) {
                log.error("❌ Failed to create payment for user {}: {}",
                        saved.getUsername(), e.getMessage(), e);
                throw new IllegalStateException(
                        "Gagal membuat transaksi pembayaran: " + e.getMessage()
                );
            }
        }

        // ============================================================
        // 9. PUBLISH EVENT (welcome email)
        // ============================================================
        if (!paymentEnabled) {
            eventPublisher.publishEvent(new UserRegisteredEvent(
                    saved.getUsername(),
                    saved.getEmail(),
                    saved.getPortfolioSlug(),
                    false
            ));
        }

        // 10. Return
        return RegisterResponse.builder()
                .message(paymentEnabled
                        ? "Registrasi berhasil! Selesaikan pembayaran untuk melanjutkan."
                        : "Registrasi berhasil! Akun kamu menunggu approval admin.")
                .userId(saved.getId())
                .username(saved.getUsername())
                .portfolioSlug(saved.getPortfolioSlug())
                .status(saved.getStatus().name())
                .payment(paymentInfo)
                .requiresPayment(paymentEnabled)
                .build();
    }

    // ============================================================
    // FORGOT PASSWORD
    // ============================================================
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Email gak terdaftar di sistem kami"
                ));

        String token = UUID.randomUUID().toString().replace("-", "");

        user.setResetToken(token);
        user.setResetTokenExpires(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        log.info("🔑 Reset password requested for {}", email);

        try {
            emailService.sendResetPasswordEmail(email, user.getUsername(), token);
        } catch (Exception e) {
            log.error("⚠️ Failed to send reset email: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public VerifyTokenResponse verifyResetToken(String token) {
        if (token == null || token.isBlank()) {
            return VerifyTokenResponse.builder()
                    .valid(false)
                    .message("Token tidak valid")
                    .build();
        }

        User user = userRepository.findByResetToken(token).orElse(null);

        if (user == null) {
            return VerifyTokenResponse.builder()
                    .valid(false)
                    .message("Token tidak ditemukan atau udah dipakai")
                    .build();
        }

        if (!user.isResetTokenValid()) {
            return VerifyTokenResponse.builder()
                    .valid(false)
                    .message("Token udah expired. Request ulang.")
                    .build();
        }

        return VerifyTokenResponse.builder()
                .valid(true)
                .message("Token valid")
                .email(maskEmail(user.getEmail()))
                .build();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Token tidak valid atau udah expired"
                ));

        if (!user.isResetTokenValid()) {
            throw new IllegalArgumentException("Token udah expired. Request ulang.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpires(null);
        userRepository.save(user);

        log.info("✅ Password reset for user: {}", user.getUsername());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }

        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    // ============================================================
    // HELPER
    // ============================================================
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