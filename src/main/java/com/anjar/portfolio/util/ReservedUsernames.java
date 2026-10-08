package com.anjar.portfolio.util;

import java.util.Set;

/**
 * Username yang tidak boleh dipakai user karena bentrok dengan
 * route frontend / endpoint backend / sistem.
 *
 * PENTING: Default portfolio username (muhammad-anjar) WAJIB ada di sini
 * supaya user lain gak bisa "bajak" halaman utama.
 */
public final class ReservedUsernames {

    private ReservedUsernames() {}

    public static final Set<String> LIST = Set.of(
            // ===== Auth & onboarding =====
            "login", "register", "logout", "signin", "signup",
            "pending-approval", "forgot-password", "reset-password",
            "verify-email", "verify", "onboarding",
            "payment", "payments", "checkout", "billing", "invoice",

            // ===== Public routes =====
            "explore", "landing", "home", "index",
            "about", "contact", "blog", "blogs",
            "project", "projects", "portfolio", "portfolios",
            "works", "work", "pricing", "features", "faq",
            "help", "support",

            // ===== Dashboard =====
            "dashboard", "admin", "administrator", "owner", "manage",
            "settings", "setting", "config", "configuration",
            "users", "user", "profile", "account", "accounts",
            "inbox", "messages", "notifications", "analytics",

            // ===== System & infra =====
            "api", "www", "mail", "ftp", "smtp", "pop", "imap",
            "cdn", "static", "assets", "public", "private", "media",
            "upload", "uploads", "download", "downloads", "files",
            "app", "apps", "mobile", "web", "admin-panel",

            // ===== Legal =====
            "terms", "privacy", "policy", "cookies", "legal",
            "tos", "dmca", "gdpr", "license",

            // ===== Common words =====
            "test", "demo", "example", "sample", "null", "undefined",
            "system", "root", "moderator",
            "me", "my", "self", "new", "create", "edit", "delete",

            // ===== Reserved untuk branding =====
            "muhammad-anjar"  // ← default portfolio username
    );

    public static boolean isReserved(String username) {
        if (username == null) return false;
        return LIST.contains(username.toLowerCase().trim());
    }
}