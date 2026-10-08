package com.anjar.portfolio.config;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserRole;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // ⭐ Wrap dengan try-catch → app tetap start walau DB error
        try {
            fixLegacyUsers();
        } catch (Exception e) {
            log.warn("⚠️ fixLegacyUsers skipped: {}", e.getMessage());
        }

        try {
            seedSuperAdmin();
        } catch (Exception e) {
            log.warn("⚠️ seedSuperAdmin skipped: {}", e.getMessage());
        }
    }

    // ============================================================
    // FIX USER LAMA (MIGRASI)
    // ============================================================
    private void fixLegacyUsers() {
        List<User> allUsers;
        try {
            allUsers = userRepository.findAll();
        } catch (Exception e) {
            log.warn("⚠️ Tabel users belum ada atau DB error, skip fixLegacyUsers");
            return;
        }

        // Fix password NULL
        List<User> usersWithoutPassword = allUsers.stream()
                .filter(u -> u.getPassword() == null || u.getPassword().isBlank())
                .toList();

        if (!usersWithoutPassword.isEmpty()) {
            String defaultHash = passwordEncoder.encode("changeme");
            usersWithoutPassword.forEach(u -> u.setPassword(defaultHash));
            userRepository.saveAll(usersWithoutPassword);

            log.warn("⚠️ Fixed {} users without password (default: 'changeme')",
                    usersWithoutPassword.size());
        }

        // Fix status NULL
        List<User> usersWithoutStatus = allUsers.stream()
                .filter(u -> u.getStatus() == null)
                .toList();

        if (!usersWithoutStatus.isEmpty()) {
            usersWithoutStatus.forEach(u -> {
                u.setStatus(UserStatus.ACTIVE);
                u.setApprovedAt(LocalDateTime.now());
            });
            userRepository.saveAll(usersWithoutStatus);

            log.info("✅ Fixed {} users without status → ACTIVE",
                    usersWithoutStatus.size());
        }
    }

    // ============================================================
    // SEED SUPER ADMIN
    // ============================================================
    private void seedSuperAdmin() {
        try {
            if (userRepository.existsByUsername("anjar")) {
                log.info("✅ Super admin user sudah ada, skip seed.");
                return;
            }
        } catch (Exception e) {
            log.warn("⚠️ Tidak bisa cek user 'anjar', skip seed.");
            return;
        }

        User superAdmin = User.builder()
                .username("anjar")
                .email("anjarfals07@gmail.com")
                .password(passwordEncoder.encode("nop4ssword"))
                .portfolioSlug("anjar")
                .displayName("Super Admin")
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .active(true)
                .approvedAt(LocalDateTime.now())
                .build();

        userRepository.save(superAdmin);

        log.info("=================================================");
        log.info("✅ Super Admin user created!");
        log.info("   Username: anjar");
        log.info("   Password: nop4ssword");
        log.info("=================================================");
    }
}