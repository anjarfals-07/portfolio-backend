package com.anjar.portfolio.config;

import com.anjar.portfolio.security.CurrentUserIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * WebMvcConfig — Konfigurasi Web MVC.
 *
 * Fungsinya:
 * 1. Daftarkan folder `uploads/` sebagai static resource
 *    → URL `/uploads/**` bisa diakses publik
 * 2. Register `CurrentUserIdResolver` untuk annotation `@CurrentUserId`
 *
 * Contoh URL:
 * - http://localhost:8080/uploads/cv/cv-2-xxxx.pdf
 * - http://localhost:8080/uploads/avatar/avatar-2-xxxx.jpg
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final CurrentUserIdResolver currentUserIdResolver;

    // ============================================================
    // STATIC RESOURCE HANDLER
    // ============================================================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = "file:" + uploadPath + "/";

        log.info("📁 Static resource: /uploads/** → {}", location);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }

    // ============================================================
    // ARGUMENT RESOLVER
    // ============================================================
    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdResolver);
        log.info("✅ Registered CurrentUserIdResolver");
    }
}