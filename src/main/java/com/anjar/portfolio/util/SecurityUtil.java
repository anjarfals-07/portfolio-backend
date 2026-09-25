package com.anjar.portfolio.util;

import com.anjar.portfolio.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility untuk ambil info current user dari SecurityContext.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // utility class
    }

    /**
     * Get current Authentication.
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get current userId. Return null kalau gak login.
     */
    public static Long getCurrentUserId() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }
        return null;
    }

    /**
     * Get current username.
     */
    public static String getCurrentUsername() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return null;
    }

    /**
     * Get current role.
     */
    public static String getCurrentRole() {
        Authentication auth = getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getRole();
        }
        return null;
    }

    /**
     * Cek apakah current user super admin.
     */
    public static boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentRole());
    }

    /**
     * Cek apakah current user owner.
     */
    public static boolean isOwner() {
        return "OWNER".equals(getCurrentRole());
    }

    /**
     * Get current user id atau throw exception kalau gak login.
     */
    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return userId;
    }
}