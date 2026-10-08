package com.anjar.portfolio.security;

import com.anjar.portfolio.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtUtil — JWT generation & validation.
 *
 * ⭐ Production-ready:
 * - Support access token + refresh token
 * - Type claim untuk bedakan access/refresh
 * - Validation dengan detail logging
 * - Handle Integer/Long userId (PostgreSQL quirks)
 */
@Slf4j
@Component
public class JwtUtil {

    // ============================================================
    // CLAIM NAMES
    // ============================================================
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    // ============================================================
    // TOKEN TYPES
    // ============================================================
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    // ============================================================
    // CONFIG
    // ============================================================
    @Value("${app.jwt.secret}")
    private String secret;

    /** Access token TTL (default: 24 jam) */
    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    /** Refresh token TTL (default: 30 hari) */
    @Value("${app.jwt.refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    // ============================================================
    // SIGNING KEY
    // ============================================================
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ============================================================
    // GENERATE TOKEN
    // ============================================================

    /**
     * Generate access token (short-lived).
     * Dipakai untuk API request.
     */
    public String generateToken(User user) {
        return generateToken(user, expirationMs, TYPE_ACCESS);
    }

    /**
     * Generate refresh token (long-lived).
     * Dipakai untuk refresh access token.
     */
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshExpirationMs, TYPE_REFRESH);
    }

    /**
     * Internal: generate token dengan TTL & type custom.
     */
    private String generateToken(User user, long ttlMs, String type) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, user.getId());
        claims.put(CLAIM_ROLE, user.getRole().name());
        claims.put(CLAIM_TYPE, type);

        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ============================================================
    // EXTRACT CLAIMS
    // ============================================================

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = parseClaims(token).get(CLAIM_USER_ID);
        if (userId == null) return null;
        if (userId instanceof Integer) return ((Integer) userId).longValue();
        if (userId instanceof Long) return (Long) userId;
        if (userId instanceof Number) return ((Number) userId).longValue();
        return null;
    }

    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public String extractType(String token) {
        return parseClaims(token).get(CLAIM_TYPE, String.class);
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    // ============================================================
    // VALIDATE
    // ============================================================

    /**
     * Validate access token (any valid JWT).
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token — cek type = "refresh".
     */
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get(CLAIM_TYPE, String.class);
            return TYPE_REFRESH.equals(type);
        } catch (Exception e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate access token — cek type = "access".
     */
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            String type = claims.get(CLAIM_TYPE, String.class);
            return TYPE_ACCESS.equals(type);
        } catch (Exception e) {
            log.warn("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    // ============================================================
    // PARSE
    // ============================================================

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public long getExpirationMs() {
        return expirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}