package com.anjar.portfolio.config;

import com.anjar.portfolio.security.CustomUserDetailsService;
import com.anjar.portfolio.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC =====
                        // Auth
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public read (GET)
                        .requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/profile").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/skills/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tech-stack/**").permitAll()

                        // Public write (contact form)
                        .requestMatchers(HttpMethod.POST, "/api/messages").permitAll()

                        // ===== ADMIN ONLY =====
                        // Profile update
                        .requestMatchers(HttpMethod.POST, "/api/profile").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/profile").hasRole("ADMIN")

                        // Projects
                        .requestMatchers(HttpMethod.POST, "/api/projects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/projects/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**").hasRole("ADMIN")

                        // Experiences
                        .requestMatchers(HttpMethod.POST, "/api/experiences/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/experiences/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/experiences/**").hasRole("ADMIN")

                        // Skills
                        .requestMatchers(HttpMethod.POST, "/api/skills/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/skills/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/skills/**").hasRole("ADMIN")

                        // Tech Stack
                        .requestMatchers(HttpMethod.POST, "/api/tech-stack/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tech-stack/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tech-stack/**").hasRole("ADMIN")

                        // Messages (admin read/delete)
                        .requestMatchers(HttpMethod.GET, "/api/messages/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/messages/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/messages/**").hasRole("ADMIN")

                        // ===== ANY OTHER =====
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}