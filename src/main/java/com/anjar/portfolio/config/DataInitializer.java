package com.anjar.portfolio.config;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Bikin admin default kalau belum ada
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@portfolio.com")
                    .passwordHash(passwordEncoder.encode("nop4ssword"))
                    .role("ADMIN")
                    .active(true)
                    .build();

            userRepository.save(admin);

            log.info("=================================================");
            log.info("✅ Admin user created!");
            log.info("   Username: admin");
            log.info("   Password: nop4ssword");
            log.info("   ⚠️  GANTI PASSWORD SETELAH LOGIN PERTAMA!");
            log.info("=================================================");
        } else {
            log.info("Admin user sudah ada, skip seed.");
        }
    }
}