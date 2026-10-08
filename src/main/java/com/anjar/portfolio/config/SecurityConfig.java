package com.anjar.portfolio.config;

import com.anjar.portfolio.security.JwtAuthFilter;
import com.anjar.portfolio.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig — URL-level security.
 *
 * ⭐ STRATEGI:
 * - /api/public/**          → PUBLIC (platform info + custom domain lookup)
 * - /api/auth/**            → PUBLIC (login/register) kecuali /me
 * - /api/payment-methods    → PUBLIC GET (list metode bayar untuk register)
 * - /api/users/**           → PUBLIC (portfolio GET)
 * - /api/webhook/**         → PUBLIC (verify di service)
 * - /api/payment/status/**  → PUBLIC (polling setelah register)
 * - /api/payment/**         → OWNER + SUPER_ADMIN
 * - /api/owner/**           → OWNER + SUPER_ADMIN (custom domain, dll)
 * - /api/admin/**           → SUPER_ADMIN only
 * - /api/me/**              → OWNER + SUPER_ADMIN
 * - /uploads/**             → PUBLIC (static files, handled by WebMvcConfig)
 * - Fallback                → authenticated
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // ============================================================
    // SECURITY FILTER CHAIN
    // ============================================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // ============================================================
                        // 1. PUBLIC — AUTH
                        // ============================================================
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-reset-token",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // ============================================================
                        // 2. ⭐ PUBLIC — PLATFORM INFO + CUSTOM DOMAIN LOOKUP
                        //    Termasuk: /api/public/tenant-by-domain
                        // ============================================================
                        .requestMatchers("/api/public/**").permitAll()

                        // ============================================================
                        // 3. PUBLIC — PAYMENT METHODS (untuk halaman register)
                        // ============================================================
                        .requestMatchers(HttpMethod.GET, "/api/payment-methods")
                        .permitAll()

                        // ============================================================
                        // 4. PUBLIC — STATIC & UTILITY
                        // ============================================================
                        .requestMatchers(
                                "/uploads/**",
                                "/error",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ============================================================
                        // 5. PUBLIC — WEBHOOK (Midtrans, dll)
                        // ============================================================
                        .requestMatchers("/api/webhook/**").permitAll()

                        // ============================================================
                        // 6. PUBLIC — PAYMENT STATUS (polling)
                        // ============================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/payment/status/**"
                        ).permitAll()

                        // ============================================================
                        // 7. PUBLIC — USER PUBLIC API (portfolio)
                        // ============================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/users/**"
                        ).permitAll()

                        // ============================================================
                        // 8. PUBLIC — CONTACT FORM (POST)
                        // ============================================================
                        .requestMatchers(HttpMethod.POST,
                                "/api/users/*/messages"
                        ).permitAll()

                        // ============================================================
                        // 9. PUBLIC — PORTFOLIO DATA (GET only)
                        // ============================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/projects/public/**",
                                "/api/skills/public/**",
                                "/api/experiences/public/**",
                                "/api/tech-stack/public/**",
                                "/api/profile/public/**",
                                "/api/blog/user/**",
                                "/api/themes/presets",
                                "/api/themes/public/**",
                                "/api/theme/presets",
                                "/api/theme/user/**"
                        ).permitAll()

                        // ============================================================
                        // 10. PUBLIC — CONTACT FORM (POST only)
                        // ============================================================
                        .requestMatchers(HttpMethod.POST,
                                "/api/messages/public/**"
                        ).permitAll()

                        // ============================================================
                        // 11. /api/auth/me — WAJIB LOGIN
                        // ============================================================
                        .requestMatchers("/api/auth/me").authenticated()

                        // ============================================================
                        // 12. ADMIN ONLY
                        // ============================================================
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")

                        // ============================================================
                        // 13. PAYMENT — OWNER + SUPER_ADMIN
                        // ============================================================
                        .requestMatchers("/api/payment/**")
                        .hasAnyRole("OWNER", "SUPER_ADMIN")

                        // ============================================================
                        // 14. ⭐ OWNER DOMAIN — OWNER + SUPER_ADMIN
                        //     Custom domain management
                        // ============================================================
                        .requestMatchers("/api/owner/**")
                        .hasAnyRole("OWNER", "SUPER_ADMIN")

                        // ============================================================
                        // 15. OWNER + SUPER_ADMIN (existing)
                        // ============================================================
                        .requestMatchers(
                                "/api/me/**",
                                "/api/themes/me/**",
                                "/api/themes/me",
                                "/api/projects/**",
                                "/api/blog/me/**",
                                "/api/skills/**",
                                "/api/experiences/**",
                                "/api/tech-stack/**",
                                "/api/profile",
                                "/api/messages"
                        ).hasAnyRole("OWNER", "SUPER_ADMIN")

                        // ============================================================
                        // 16. FALLBACK
                        // ============================================================
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.setCharacterEncoding("UTF-8");
                            res.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\",\"status\":401}"
                            );
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.setCharacterEncoding("UTF-8");
                            res.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"Access denied\",\"status\":403}"
                            );
                        })
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // AUTH PROVIDER
    // ============================================================
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================================================
    // CORS
    // ============================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}