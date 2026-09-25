package com.anjar.portfolio.config;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.entity.UserRole;
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
        if (!userRepository.existsByUsername("anjar")) {
            User admin = User.builder()
                    .username("anjar")
                    .email("anjarfals07@gmail.com")
                    .password(passwordEncoder.encode("nop4ssword"))
                    .portfolioSlug("anjar")
                    .displayName("Super Admin")
                    .role(UserRole.SUPER_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);

            log.info("=================================================");
            log.info("✅ Super Admin user created!");
            log.info("   Username: anjar");
            log.info("   Password: nop4ssword");
            log.info("   Slug: anjar");
            log.info("   ⚠️  GANTI PASSWORD SETELAH LOGIN PERTAMA!");
            log.info("=================================================");
        } else {
            log.info("Super admin user sudah ada, skip seed.");
        }
    }
}