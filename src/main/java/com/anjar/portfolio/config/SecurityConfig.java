package com.anjar.portfolio.config;

import com.anjar.portfolio.security.JwtAuthFilter;
import com.anjar.portfolio.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC =====
                        .requestMatchers(
                                "/api/auth/**",

                                // Public portfolio user
                                "/api/users/**",

                                // Public endpoints (prefix /public/)
                                "/api/projects/public/**",
                                "/api/skills/public/**",
                                "/api/experiences/public/**",
                                "/api/tech-stack/public/**",
                                "/api/profile/public/**",

                                // Public blog
                                "/api/blog/user/**",

                                // Public contact form
                                "/api/messages/public/**"
                        ).permitAll()

                        .requestMatchers(
                                "/error",
                                "/actuator/health"
                        ).permitAll()

                        // ===== SUPER ADMIN ONLY =====
                        .requestMatchers("/api/admin/**")
                        .hasRole("SUPER_ADMIN")

                        // ===== OWNER (authenticated) =====
                        .requestMatchers("/api/me/**")
                        .hasAnyRole("OWNER", "SUPER_ADMIN")

                        // Owner endpoints (yang pakai /api/xxx tanpa /me/)
                        .requestMatchers(
                                "/api/projects/**",
                                "/api/blog/me/**",
                                "/api/skills/**",
                                "/api/experiences/**",
                                "/api/tech-stack/**",
                                "/api/profile",
                                "/api/messages"
                        ).hasAnyRole("OWNER", "SUPER_ADMIN")

                        // ===== FALLBACK =====
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}