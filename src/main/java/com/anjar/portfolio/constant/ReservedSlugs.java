package com.anjar.portfolio.constant;

import java.util.Set;

/**
 * Slug yang tidak boleh dipakai user karena konflik dengan route sistem.
 *
 * Contoh: user gak bisa daftar dengan slug "admin" karena bakal bentrok
 * dengan /admin route.
 */
public final class ReservedSlugs {

    private ReservedSlugs() {
        // utility class — gak boleh diinstansiasi
    }

    /**
     * Set semua slug terlarang (lowercase).
     *
     * ⚠️ PENTING: Gak boleh ada duplikat — Set.of() throw error kalau duplikat.
     */
    public static final Set<String> RESERVED = Set.of(
            // ===== Auth & Admin =====
            "admin",
            "administrator",
            "login",
            "register",
            "signup",
            "signin",
            "logout",
            "auth",
            "dashboard",
            "settings",

            // ===== API & System =====
            "api",
            "me",
            "user",
            "users",
            "profile",
            "account",
            "accounts",

            // ===== Public Pages =====
            "home",
            "about",
            "contact",
            "projects",
            "blog",
            "portfolio",
            "portfolios",
            "works",
            "resume",
            "cv",

            // ===== Static & Assets =====
            "assets",
            "static",
            "public",
            "images",
            "img",
            "css",
            "js",
            "media",
            "files",
            "uploads",
            "favicon",
            "robots",
            "sitemap",

            // ===== Reserved Words =====
            "app",
            "web",
            "site",
            "www",
            "mail",
            "ftp",
            "cpanel",
            "webmail",
            "ns1",
            "ns2",
            "test",
            "demo",
            "example",

            // ===== Legal =====
            "terms",
            "privacy",
            "policy",
            "help",
            "support",
            "faq",
            "docs",
            "documentation",
            "pricing",

            // ===== Common Routes =====
            "new",
            "edit",
            "create",
            "update",
            "delete",
            "search"
    );

    /**
     * Cek apakah slug adalah reserved (case-insensitive).
     */
    public static boolean isReserved(String slug) {
        if (slug == null || slug.isBlank()) return true;
        return RESERVED.contains(slug.toLowerCase().trim());
    }

    /**
     * Tambah method untuk debug: tampilkan semua reserved slug.
     * Useful buat development.
     */
    public static Set<String> getAll() {
        return RESERVED;
    }
}