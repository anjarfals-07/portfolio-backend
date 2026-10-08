package com.anjar.portfolio.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter.
 *
 * ⭐ FIX:
 * - Set principal sebagai UserDetailsImpl (bukan CurrentUser)
 *   biar konsisten dengan Spring Security standard
 * - Return 401 kalau token invalid/expired
 * - Return 401 kalau header format salah
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ============================================================
        // 1. TIDAK ada header → lanjut (public routes)
        // ============================================================
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ============================================================
        // 2. FORMAT salah → 401
        // ============================================================
        if (!authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ Invalid Authorization header: {}",
                    request.getRequestURI());
            sendUnauthorized(response, "Invalid Authorization header format");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            sendUnauthorized(response, "Empty token");
            return;
        }

        // ============================================================
        // 3. ⭐ VALIDATE — return 401 kalau invalid/expired
        // ============================================================
        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("🔒 Invalid JWT for {} {}",
                        request.getMethod(), request.getRequestURI());
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (username == null || userId == null || role == null) {
                sendUnauthorized(response, "Invalid token claims");
                return;
            }

            // ============================================================
            // 4. Set authentication — pakai UserDetailsImpl
            // ============================================================
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetailsImpl userDetails = new UserDetailsImpl(userId, username, role);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                log.debug("✅ Authenticated: {} (id={}, role={}, path={})",
                        username, userId, role, request.getRequestURI());
            }

        } catch (ExpiredJwtException e) {
            log.warn("⏰ Expired JWT for {} {}",
                    request.getMethod(), request.getRequestURI());
            sendUnauthorized(response, "Token expired. Please login again.");
            return;

        } catch (JwtException e) {
            log.warn("🔒 JWT error for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            sendUnauthorized(response, "Invalid token");
            return;

        } catch (Exception e) {
            log.error("❌ Auth error for {} {}: {}",
                    request.getMethod(), request.getRequestURI(), e.getMessage());
            sendUnauthorized(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
        );
    }
}