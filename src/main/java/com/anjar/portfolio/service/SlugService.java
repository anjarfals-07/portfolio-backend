package com.anjar.portfolio.service;

import com.anjar.portfolio.constant.ReservedSlugs;
import com.anjar.portfolio.exception.SlugAlreadyExistsException;
import com.anjar.portfolio.repository.UserRepository;
import com.anjar.portfolio.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlugService {

    private final UserRepository userRepository;

    private static final int MAX_ATTEMPTS = 100;

    /**
     * Generate slug unik dari input (username atau displayName).
     *
     * Flow:
     *   1. Convert input ke slug format
     *   2. Cek reserved → kalau reserved, throw exception
     *   3. Cek existing di DB → kalau ada, tambah suffix (-2, -3, ...)
     *   4. Return slug yang unique
     *
     * @param input      string input (username atau displayName)
     * @param allowAutoSuffix  kalau true, auto-tambah suffix kalau bentrok
     *                         kalau false, throw exception kalau bentrok
     * @return slug unik
     */
    public String generateUniqueSlug(String input, boolean allowAutoSuffix) {
        String baseSlug = SlugUtil.toSlug(input);

        if (baseSlug == null) {
            throw new IllegalArgumentException("Input tidak bisa di-generate jadi slug");
        }

        // Validate format
        SlugUtil.validateSlug(baseSlug);

        // Cek reserved
        if (ReservedSlugs.isReserved(baseSlug)) {
            throw new SlugAlreadyExistsException(
                    baseSlug,
                    "adalah kata yang direserve sistem. Coba username/nama lain."
            );
        }

        // Cek existing
        if (!userRepository.existsByPortfolioSlug(baseSlug)) {
            log.info("Slug '{}' available", baseSlug);
            return baseSlug;
        }

        // Kalau udah ada, cek apakah boleh auto-suffix
        if (!allowAutoSuffix) {
            throw new SlugAlreadyExistsException(baseSlug, "sudah dipakai user lain");
        }

        // Auto-increment: coba budi-2, budi-3, ...
        for (int i = 2; i <= MAX_ATTEMPTS; i++) {
            String candidate = SlugUtil.addSuffix(baseSlug, i);

            // Re-validate (karena bisa kepotong)
            if (!SlugUtil.isValidSlug(candidate)) {
                continue;
            }

            // Cek reserved (untuk safety)
            if (ReservedSlugs.isReserved(candidate)) {
                continue;
            }

            // Cek existing
            if (!userRepository.existsByPortfolioSlug(candidate)) {
                log.info("Slug '{}' taken, using '{}'", baseSlug, candidate);
                return candidate;
            }
        }

        throw new SlugAlreadyExistsException(
                baseSlug,
                "sudah terlalu banyak variannya. Coba username/nama lain."
        );
    }

    /**
     * Generate slug unik dengan auto-suffix (default).
     */
    public String generateUniqueSlug(String input) {
        return generateUniqueSlug(input, true);
    }

    /**
     * Validate slug manual yang diinput user (misal dari form admin).
     *
     * Cek:
     * - Format valid
     * - Gak reserved
     * - Gak bentrok dengan user lain (kecuali diri sendiri)
     *
     * @param slug      slug yang mau dicek
     * @param excludeUserId  userId yang di-exclude (untuk update user existing)
     */
    public void validateSlugForUser(String slug, Long excludeUserId) {
        // Format
        if (!SlugUtil.isValidSlug(slug)) {
            throw new IllegalArgumentException(
                    "Slug tidak valid. 3-50 karakter, lowercase, alfanumerik & dash."
            );
        }

        // Reserved
        if (ReservedSlugs.isReserved(slug)) {
            throw new SlugAlreadyExistsException(
                    slug,
                    "adalah kata yang direserve sistem."
            );
        }

        // Conflict dengan user lain
        userRepository.findByPortfolioSlug(slug)
                .ifPresent(existingUser -> {
                    // Kalau user existing bukan diri sendiri → conflict
                    if (excludeUserId == null || !existingUser.getId().equals(excludeUserId)) {
                        throw new SlugAlreadyExistsException(slug, "sudah dipakai user lain");
                    }
                });
    }

    /**
     * Validate slug untuk user baru (tanpa exclude).
     */
    public void validateSlugForNewUser(String slug) {
        validateSlugForUser(slug, null);
    }
}