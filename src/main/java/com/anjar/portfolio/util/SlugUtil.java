package com.anjar.portfolio.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility untuk generate & validate slug.
 */
public final class SlugUtil {

    private SlugUtil() {
        // utility class
    }

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MULTIPLE_DASH = Pattern.compile("-+");
    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    /**
     * Generate slug dari input string.
     *
     * Contoh:
     *   "Budi Santoso"      → "budi-santoso"
     *   "Anjar Fals"        → "anjar-fals"
     *   "Héllo Wörld!!"     → "hello-world"
     *   "  spaces  here  "  → "spaces-here"
     *   "a_b_c"             → "a-b-c"
     *
     * @param input string yang mau di-slug
     * @return slug (lowercase, dash-separated) atau null kalau invalid
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        // Normalize unicode (é → e, ñ → n, dll)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");

        // Lowercase
        String slug = normalized.toLowerCase().trim();

        // Replace non-alphanumeric dengan dash
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll("-");

        // Collapse multiple dash
        slug = MULTIPLE_DASH.matcher(slug).replaceAll("-");

        // Trim leading/trailing dash
        slug = slug.replaceAll("^-+|-+$", "");

        // Truncate ke max length
        if (slug.length() > MAX_LENGTH) {
            slug = slug.substring(0, MAX_LENGTH);
            // Trim trailing dash after truncate
            slug = slug.replaceAll("-+$", "");
        }

        // Kalau kosong setelah cleaning, return null
        if (slug.isEmpty()) {
            return null;
        }

        return slug;
    }

    /**
     * Validate apakah string adalah slug valid.
     *
     * Rules:
     * - Lowercase only
     * - Alphanumeric + dash
     * - Dash gak boleh di awal/akhir
     * - Gak boleh ada dash beruntun
     * - Length 3-50 karakter
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }

        if (slug.length() < MIN_LENGTH || slug.length() > MAX_LENGTH) {
            return false;
        }

        return SLUG_PATTERN.matcher(slug).matches();
    }

    /**
     * Validate + throw exception kalau invalid.
     */
    public static void validateSlug(String slug) {
        if (!isValidSlug(slug)) {
            throw new IllegalArgumentException(
                    "Slug tidak valid. Harus 3-50 karakter, lowercase, alfanumerik & dash."
            );
        }
    }

    /**
     * Tambah suffix angka ke slug.
     *
     * Contoh:
     *   addSuffix("budi", 2) → "budi-2"
     *   addSuffix("budi", 10) → "budi-10"
     */
    public static String addSuffix(String baseSlug, int suffix) {
        if (baseSlug == null) return null;
        if (suffix <= 1) return baseSlug;

        String suffixStr = "-" + suffix;
        int maxBaseLength = MAX_LENGTH - suffixStr.length();

        String trimmedBase = baseSlug;
        if (trimmedBase.length() > maxBaseLength) {
            trimmedBase = trimmedBase.substring(0, maxBaseLength);
            trimmedBase = trimmedBase.replaceAll("-+$", "");
        }

        return trimmedBase + suffixStr;
    }
}