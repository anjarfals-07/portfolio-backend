package com.anjar.portfolio.util;

import com.anjar.portfolio.security.CurrentUser;
import com.anjar.portfolio.security.UserDetailsImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility untuk ambil info current user dari SecurityContext.
 *
 * Support 2 tipe principal:
 * 1. UserDetailsImpl  — dari form login (UsernamePasswordAuthenticationToken)
 * 2. CurrentUser      — dari JwtAuthFilter (JWT token)
 *
 * Prinsip:
 * - Kalau belum login / principal invalid → throw BadCredentialsException
 *   (akan di-handle jadi 401, bukan 500).
 * - Kalau butuh value tanpa throw → pakai getXxxOrNull().
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // utility class
    }

    // ============================================================
    // AUTH HELPER
    // ============================================================

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Cek apakah user benar-benar terautentikasi (bukan anonymous).
     */
    public static boolean isAuthenticated() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;

        Object principal = auth.getPrincipal();
        if (principal == null) return false;
        if ("anonymousUser".equals(principal)) return false;

        return true;
    }

    // ============================================================
    // USER ID
    // ============================================================

    /**
     * Get current userId (nullable).
     * Return null kalau gak login atau principal tidak dikenal.
     */
    public static Long getCurrentUserIdOrNull() {
        if (!isAuthenticated()) return null;

        Object principal = getAuthentication().getPrincipal();

        // ===== Case 1: UserDetailsImpl (dari form login) =====
        if (principal instanceof UserDetailsImpl impl) {
            return impl.getId();
        }

        // ===== Case 2: CurrentUser (dari JwtAuthFilter) =====
        if (principal instanceof CurrentUser cu) {
            return cu.getId();
        }

        // ===== Case 3: Fallback via reflection (getId()) =====
        try {
            var m = principal.getClass().getMethod("getId");
            Object id = m.invoke(principal);
            if (id instanceof Long l) return l;
            if (id instanceof Integer i) return i.longValue();
        } catch (Exception ignored) {
            // lanjut
        }

        return null;
    }

    /**
     * Get current userId atau throw BadCredentialsException.
     * Cocok untuk endpoint yang WAJIB login.
     */
    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BadCredentialsException("User not authenticated");
        }
        return userId;
    }

    // ============================================================
    // USERNAME
    // ============================================================

    public static String getCurrentUsernameOrNull() {
        if (!isAuthenticated()) return null;

        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl impl) {
            return impl.getUsername();
        }
        if (principal instanceof CurrentUser cu) {
            return cu.getUsername();
        }
        if (principal instanceof UserDetails ud) {
            return ud.getUsername();
        }
        if (principal instanceof String s) {
            return s;
        }

        return null;
    }

    public static String requireCurrentUsername() {
        String u = getCurrentUsernameOrNull();
        if (u == null) {
            throw new BadCredentialsException("User not authenticated");
        }
        return u;
    }

    // ============================================================
    // ROLE
    // ============================================================

    public static String getCurrentRoleOrNull() {
        if (!isAuthenticated()) return null;

        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl impl) {
            return impl.getRole();
        }
        if (principal instanceof CurrentUser cu) {
            return cu.getRole();
        }

        // Fallback via authorities
        Authentication auth = getAuthentication();
        if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            String authority = auth.getAuthorities().iterator().next().getAuthority();
            if (authority.startsWith("ROLE_")) {
                return authority.substring(5);
            }
            return authority;
        }

        return null;
    }

    public static boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentRoleOrNull());
    }

    public static boolean isOwner() {
        return "OWNER".equals(getCurrentRoleOrNull());
    }
}