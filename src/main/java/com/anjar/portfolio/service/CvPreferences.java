package com.anjar.portfolio.service;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * CV Preferences — hasil parsing dari JSONB profile.cvPreferences.
 *
 * Konsep:
 * - User isi JSONB dengan pilihan (palette, fontPair, layout, dll)
 * - Class ini nerjemahin jadi token konkret (accentColor, fontHeading, dll)
 * - Hasilnya di-inject ke Thymeleaf sebagai CSS variables
 *
 * Prioritas resolver:
 *   1. Custom override (accentColor, fontHeading, fontBody)
 *   2. Preset (palette, fontPair)
 *   3. Default (ocean, inter-inter)
 */
@Slf4j
public class CvPreferences {

    // ============================================================
    // CORE
    // ============================================================
    public String template = "modern";
    public String layout   = "sidebar-left";
    public String theme    = "light";
    public String density  = "normal";

    // ============================================================
    // VISUAL
    // ============================================================
    public String palette           = "ocean";
    public String accentColor       = "#3b82f6";
    public String fontPair          = "inter-inter";
    public String fontHeading       = "Inter";
    public String fontBody          = "Inter";
    public String iconSet           = "primeicons";
    public String cardStyle         = "soft";
    public String badgeStyle        = "pill";
    public String backgroundPattern = "none";

    // ============================================================
    // SECTION TOGGLE
    // ============================================================
    public boolean showBio            = true;
    public boolean showPersonalInfo   = true;   // ⭐ BARU
    public boolean showWorkExperience = true;   // ⭐ BARU
    public boolean showEducation      = true;   // ⭐ BARU
    public boolean showExperiences    = true;
    public boolean showProjects       = true;
    public boolean showSkills         = true;
    public boolean showTechStack      = true;

    // ============================================================
    // VALID SETS (untuk validasi input)
    // ============================================================
    public static final Set<String> VALID_TEMPLATES = Set.of("modern", "classic", "minimal");
    public static final Set<String> VALID_LAYOUTS   = Set.of("sidebar-left", "sidebar-right", "header-top", "two-col", "timeline");
    public static final Set<String> VALID_THEMES    = Set.of("light", "dark");
    public static final Set<String> VALID_DENSITY   = Set.of("compact", "normal", "spacious");
    public static final Set<String> VALID_ICON_SETS = Set.of("primeicons", "lucide", "none");
    public static final Set<String> VALID_CARDS     = Set.of("soft", "flat", "outline", "glass", "none");
    public static final Set<String> VALID_BADGES    = Set.of("pill", "square", "outline", "minimal");
    public static final Set<String> VALID_PATTERNS  = Set.of("none", "dots", "lines", "mesh");

    // ============================================================
    // PALETTE PRESETS (12 warna)
    // ============================================================
    public record Palette(String accent, String accentDark) {}

    public static final Map<String, Palette> PALETTES = new LinkedHashMap<>() {{
        put("ocean",    new Palette("#3b82f6", "#1e40af"));
        put("sunset",   new Palette("#f97316", "#9a3412"));
        put("forest",   new Palette("#10b981", "#065f46"));
        put("royal",    new Palette("#8b5cf6", "#5b21b6"));
        put("rose",     new Palette("#f43f5e", "#9f1239"));
        put("slate",    new Palette("#475569", "#1e293b"));
        put("amber",    new Palette("#f59e0b", "#78350f"));
        put("teal",     new Palette("#14b8a6", "#134e4a"));
        put("indigo",   new Palette("#6366f1", "#3730a3"));
        put("crimson",  new Palette("#dc2626", "#7f1d1d"));
        put("midnight", new Palette("#0f172a", "#020617"));
        put("mint",     new Palette("#34d399", "#047857"));
    }};

    // ============================================================
    // FONT PAIRING PRESETS (8 pairing)
    // ============================================================
    public record FontPair(String heading, String body) {}

    public static final Map<String, FontPair> FONT_PAIRS = new LinkedHashMap<>() {{
        put("inter-inter",         new FontPair("Inter", "Inter"));
        put("inter-lora",          new FontPair("Inter", "Lora"));
        put("playfair-source",     new FontPair("Playfair Display", "Source Sans 3"));
        put("montserrat-roboto",   new FontPair("Montserrat", "Roboto"));
        put("poppins-open",        new FontPair("Poppins", "Open Sans"));
        put("dm-serif-dm-sans",    new FontPair("DM Serif Display", "DM Sans"));
        put("merriweather-georgia",new FontPair("Merriweather", "Georgia"));
        put("jetbrains-inter",     new FontPair("JetBrains Mono", "Inter"));
    }};

    // ============================================================
    // PARSE — entry point
    // ============================================================
    public static CvPreferences parse(Map<String, Object> raw) {
        CvPreferences p = new CvPreferences();
        if (raw == null || raw.isEmpty()) return p;

        // --- Enum-ish fields (validated) ---
        p.template           = validated(raw, "template", p.template, VALID_TEMPLATES);
        p.layout             = validated(raw, "layout", p.layout, VALID_LAYOUTS);
        p.theme              = validated(raw, "theme", p.theme, VALID_THEMES);
        p.density            = validated(raw, "density", p.density, VALID_DENSITY);
        p.iconSet            = validated(raw, "iconSet", p.iconSet, VALID_ICON_SETS);
        p.cardStyle          = validated(raw, "cardStyle", p.cardStyle, VALID_CARDS);
        p.badgeStyle         = validated(raw, "badgeStyle", p.badgeStyle, VALID_BADGES);
        p.backgroundPattern  = validated(raw, "backgroundPattern", p.backgroundPattern, VALID_PATTERNS);

        // --- Palette ---
        p.palette = str(raw, "palette", p.palette).toLowerCase();
        String customAccent = str(raw, "accentColor", null);
        if (customAccent != null && isValidHex(customAccent)) {
            p.accentColor = customAccent;
        } else {
            Palette pal = PALETTES.getOrDefault(p.palette, PALETTES.get("ocean"));
            p.accentColor = pal.accent();
        }

        // --- Font ---
        p.fontPair = str(raw, "fontPair", p.fontPair);
        FontPair fp = FONT_PAIRS.getOrDefault(p.fontPair, FONT_PAIRS.get("inter-inter"));
        p.fontHeading = str(raw, "fontHeading", fp.heading());
        p.fontBody    = str(raw, "fontBody", fp.body());

        // --- Toggles (existing) ---
        p.showBio         = bool(raw, "showBio", true);
        p.showExperiences = bool(raw, "showExperiences", true);
        p.showProjects    = bool(raw, "showProjects", true);
        p.showSkills      = bool(raw, "showSkills", true);
        p.showTechStack   = bool(raw, "showTechStack", true);

        // --- Toggles (BARU) ---
        p.showPersonalInfo   = bool(raw, "showPersonalInfo", true);
        p.showWorkExperience = bool(raw, "showWorkExperience", true);
        p.showEducation      = bool(raw, "showEducation", true);

        return p;
    }

    // ============================================================
    // EXPORT untuk Thymeleaf
    // ============================================================
    public Map<String, Object> toTemplateVars() {
        Map<String, Object> v = new HashMap<>();
        v.put("layout", layout);
        v.put("theme", theme);
        v.put("density", density);
        v.put("accentColor", accentColor);
        v.put("accentColorDark", deriveDark(accentColor));
        v.put("accentColorLight", deriveLight(accentColor));
        v.put("fontHeading", fontHeading);
        v.put("fontBody", fontBody);
        v.put("iconSet", iconSet);
        v.put("cardStyle", cardStyle);
        v.put("badgeStyle", badgeStyle);
        v.put("backgroundPattern", backgroundPattern);
        v.put("googleFontsUrl", buildGoogleFontsUrl(fontHeading, fontBody));

        v.put("spaceScale", switch (density) {
            case "compact"  -> "0.85";
            case "spacious" -> "1.25";
            default         -> "1.0";
        });

        // Section toggles (existing)
        v.put("showBio", showBio);
        v.put("showExperiences", showExperiences);
        v.put("showProjects", showProjects);
        v.put("showSkills", showSkills);
        v.put("showTechStack", showTechStack);

        // Section toggles (BARU)
        v.put("showPersonalInfo", showPersonalInfo);
        v.put("showWorkExperience", showWorkExperience);
        v.put("showEducation", showEducation);

        return v;
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private static String str(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return (v instanceof String s && !s.isBlank()) ? s.trim() : def;
    }

    private static boolean bool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return def;
    }

    private static String validated(Map<String, Object> m, String k, String def, Set<String> valid) {
        String v = str(m, k, def).toLowerCase();
        if (!valid.contains(v)) {
            log.warn("⚠️ Invalid {}='{}', fallback to '{}'", k, v, def);
            return def;
        }
        return v;
    }

    private static boolean isValidHex(String hex) {
        return hex != null && hex.matches("^#[0-9a-fA-F]{6}$");
    }

    // ============================================================
    // COLOR DERIVATION
    // ============================================================
    public static String deriveDark(String hex) {
        if (!isValidHex(hex)) return "#1e293b";
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            r = (int) (r * 0.4);
            g = (int) (g * 0.4);
            b = (int) (b * 0.4);
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return "#1e293b";
        }
    }

    public static String deriveLight(String hex) {
        if (!isValidHex(hex)) return "#eff6ff";
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            r = (int) (r * 0.1 + 255 * 0.9);
            g = (int) (g * 0.1 + 255 * 0.9);
            b = (int) (b * 0.1 + 255 * 0.9);
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return "#eff6ff";
        }
    }

    // ============================================================
    // GOOGLE FONTS URL BUILDER
    // ============================================================
    private String buildGoogleFontsUrl(String heading, String body) {
        String h = heading.replace(" ", "+");
        String b = body.replace(" ", "+");
        if (h.equalsIgnoreCase(b)) {
            return "https://fonts.googleapis.com/css2?family=" + h
                    + ":wght@300;400;500;600;700;800&display=swap";
        }
        return "https://fonts.googleapis.com/css2?family=" + h
                + ":wght@400;600;700;800&family=" + b
                + ":wght@300;400;500;600&display=swap";
    }
}