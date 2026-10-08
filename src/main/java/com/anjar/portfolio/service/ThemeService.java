package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.ThemeDTO;
import com.anjar.portfolio.entity.Theme;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.ThemeLayout;
import com.anjar.portfolio.enums.ThemeMode;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.ThemeRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    // ============================================================
    // PRESETS — 6 preset theme
    // ============================================================

    private static final List<Map<String, Object>> PRESETS = List.of(
            Map.of(
                    "name", "modern-blue",
                    "label", "Modern Blue",
                    "description", "Cocok untuk developer & tech",
                    "colors", Map.of(
                            "primaryColor", "#3b82f6",
                            "accentColor", "#8b5cf6",
                            "bgColor", "#ffffff",
                            "textColor", "#1e293b"
                    )
            ),
            Map.of(
                    "name", "emerald-fresh",
                    "label", "Emerald Fresh",
                    "description", "Cocok untuk kreatif & design",
                    "colors", Map.of(
                            "primaryColor", "#10b981",
                            "accentColor", "#06b6d4",
                            "bgColor", "#ffffff",
                            "textColor", "#1e293b"
                    )
            ),
            Map.of(
                    "name", "sunset-orange",
                    "label", "Sunset Orange",
                    "description", "Cocok untuk marketing & portfolio",
                    "colors", Map.of(
                            "primaryColor", "#f59e0b",
                            "accentColor", "#ef4444",
                            "bgColor", "#ffffff",
                            "textColor", "#1e293b"
                    )
            ),
            Map.of(
                    "name", "purple-haze",
                    "label", "Purple Haze",
                    "description", "Cocok untuk artist & creator",
                    "colors", Map.of(
                            "primaryColor", "#8b5cf6",
                            "accentColor", "#ec4899",
                            "bgColor", "#ffffff",
                            "textColor", "#1e293b"
                    )
            ),
            Map.of(
                    "name", "monochrome",
                    "label", "Monochrome",
                    "description", "Minimalis & profesional",
                    "colors", Map.of(
                            "primaryColor", "#1e293b",
                            "accentColor", "#64748b",
                            "bgColor", "#ffffff",
                            "textColor", "#0f172a"
                    )
            ),
            Map.of(
                    "name", "cyberpunk",
                    "label", "Cyberpunk",
                    "description", "Gamer & futuristic",
                    "colors", Map.of(
                            "primaryColor", "#00ffff",
                            "accentColor", "#ff00ff",
                            "bgColor", "#0a0a0a",
                            "textColor", "#ffffff"
                    )
            )
    );

    // ============================================================
    // GET
    // ============================================================

    /**
     * Get theme by user ID.
     * Kalau belum ada, return null (frontend pakai default).
     * ⭐ Include username + displayName untuk white-label.
     */
    @Transactional(readOnly = true)
    public ThemeDTO getTheme(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        ThemeDTO dto = themeRepository.findByUserId(userId)
                .map(this::toDTO)
                .orElse(null);

        // ⭐ Inject owner info (walau dto null, tetap buat baru)
        if (dto == null) {
            dto = new ThemeDTO();
        }
        dto.setUsername(user.getUsername());
        dto.setDisplayName(resolveDisplayName(user));

        return dto;
    }

    /**
     * Get public theme by portfolio slug.
     * Dipakai di public endpoint /api/users/{username}/theme
     *
     * ⭐ White-label: include username + displayName.
     */
    @Transactional(readOnly = true)
    public ThemeDTO getPublicTheme(String portfolioSlug) {
        // Cari user dulu (untuk displayName)
        User user = userRepository.findByPortfolioSlug(portfolioSlug)
                .or(() -> userRepository.findByUsername(portfolioSlug))
                .orElse(null);

        // Kalau user tidak ada → return null (frontend handle 404)
        if (user == null) {
            return null;
        }

        ThemeDTO dto = themeRepository.findByUserPortfolioSlug(portfolioSlug)
                .map(this::toDTO)
                .orElse(null);

        // ⭐ Inject owner info — tetap diisi walau theme null
        if (dto == null) {
            dto = new ThemeDTO();
        }
        dto.setUsername(user.getUsername());
        dto.setDisplayName(resolveDisplayName(user));

        return dto;
    }

    /**
     * Get list preset theme.
     */
    public List<Map<String, Object>> getPresets() {
        return PRESETS;
    }

    // ============================================================
    // SAVE (upsert)
    // ============================================================

    /**
     * Save theme — upsert.
     *
     * Kalau user belum punya theme → create baru.
     * Kalau udah punya → update.
     * ⭐ Include username + displayName di response.
     */
    @Transactional
    public ThemeDTO saveTheme(Long userId, ThemeDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Theme theme = themeRepository.findByUserId(userId).orElse(null);

        if (theme == null) {
            // Create baru
            theme = Theme.builder()
                    .user(user)
                    .primaryColor(dto.getPrimaryColor())
                    .accentColor(dto.getAccentColor())
                    .bgColor(dto.getBgColor())
                    .textColor(dto.getTextColor())
                    .fontFamily(dto.getFontFamily())
                    .headingFont(dto.getHeadingFont())
                    .borderRadius(dto.getBorderRadius())
                    .logoIcon(dto.getLogoIcon() != null ? dto.getLogoIcon() : "pi pi-code")
                    .layout(dto.getLayout() != null
                            ? ThemeLayout.valueOf(dto.getLayout())
                            : ThemeLayout.GRID)
                    .defaultMode(dto.getDefaultMode() != null
                            ? ThemeMode.valueOf(dto.getDefaultMode())
                            : ThemeMode.LIGHT)
                    .preset(dto.getPreset())
                    .build();

            log.info("✅ Theme created for user {} (id={})",
                    user.getUsername(), userId);
        } else {
            // Update existing
            if (dto.getPrimaryColor() != null) theme.setPrimaryColor(dto.getPrimaryColor());
            if (dto.getAccentColor() != null) theme.setAccentColor(dto.getAccentColor());
            if (dto.getBgColor() != null) theme.setBgColor(dto.getBgColor());
            if (dto.getTextColor() != null) theme.setTextColor(dto.getTextColor());
            if (dto.getFontFamily() != null) theme.setFontFamily(dto.getFontFamily());
            if (dto.getHeadingFont() != null) theme.setHeadingFont(dto.getHeadingFont());
            if (dto.getBorderRadius() != null) theme.setBorderRadius(dto.getBorderRadius());
            if (dto.getLogoIcon() != null) theme.setLogoIcon(dto.getLogoIcon());
            if (dto.getLayout() != null) theme.setLayout(ThemeLayout.valueOf(dto.getLayout()));
            if (dto.getDefaultMode() != null) theme.setDefaultMode(ThemeMode.valueOf(dto.getDefaultMode()));
            if (dto.getPreset() != null) theme.setPreset(dto.getPreset());

            log.info("✅ Theme updated for user {} (id={})",
                    user.getUsername(), userId);
        }

        // ⭐ Save + inject owner info
        Theme saved = themeRepository.save(theme);
        ThemeDTO result = toDTO(saved);
        result.setUsername(user.getUsername());
        result.setDisplayName(resolveDisplayName(user));

        return result;
    }

    // ============================================================
    // APPLY PRESET
    // ============================================================

    /**
     * Apply preset theme — set warna dari preset.
     * ⭐ Include username + displayName di response.
     */
    @Transactional
    public ThemeDTO applyPreset(Long userId, String presetName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Cari preset
        Map<String, Object> preset = PRESETS.stream()
                .filter(p -> p.get("name").equals(presetName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Preset '" + presetName + "' tidak ditemukan"
                ));

        @SuppressWarnings("unchecked")
        Map<String, String> colors = (Map<String, String>) preset.get("colors");

        Theme theme = themeRepository.findByUserId(userId).orElse(null);

        if (theme == null) {
            theme = Theme.builder()
                    .user(user)
                    .build();
        }

        // Set warna dari preset
        theme.setPrimaryColor(colors.get("primaryColor"));
        theme.setAccentColor(colors.get("accentColor"));
        theme.setBgColor(colors.get("bgColor"));
        theme.setTextColor(colors.get("textColor"));
        theme.setPreset(presetName);

        log.info("✅ Preset '{}' applied for user {} (id={})",
                presetName, user.getUsername(), userId);

        // ⭐ Save + inject owner info
        Theme saved = themeRepository.save(theme);
        ThemeDTO result = toDTO(saved);
        result.setUsername(user.getUsername());
        result.setDisplayName(resolveDisplayName(user));

        return result;
    }

    // ============================================================
    // RESET
    // ============================================================

    /**
     * Reset theme ke default (hapus theme).
     */
    @Transactional
    public void resetTheme(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        themeRepository.findByUserId(userId).ifPresent(theme -> {
            themeRepository.delete(theme);
            log.info("✅ Theme reset for user {} (id={})",
                    user.getUsername(), userId);
        });
    }

    /**
     * Cek apakah user punya theme.
     * Berguna untuk validasi.
     */
    @Transactional(readOnly = true)
    public boolean hasTheme(Long userId) {
        return themeRepository.findByUserId(userId).isPresent();
    }

    // ============================================================
    // MAPPER
    // ============================================================

    /**
     * Mapping Theme entity → ThemeDTO.
     * TIDAK include owner info — di-set oleh caller.
     */
    private ThemeDTO toDTO(Theme t) {
        return ThemeDTO.builder()
                .id(t.getId())
                .primaryColor(t.getPrimaryColor())
                .accentColor(t.getAccentColor())
                .bgColor(t.getBgColor())
                .textColor(t.getTextColor())
                .fontFamily(t.getFontFamily())
                .headingFont(t.getHeadingFont())
                .borderRadius(t.getBorderRadius())
                .logoIcon(t.getLogoIcon())
                .layout(t.getLayout() != null ? t.getLayout().name() : null)
                .defaultMode(t.getDefaultMode() != null ? t.getDefaultMode().name() : null)
                .preset(t.getPreset())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    /**
     * ⭐ Resolve displayName.
     * Prioritas:
     * 1. User.displayName (kalau ada & tidak blank)
     * 2. User.username (fallback)
     */
    private String resolveDisplayName(User user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        return user.getUsername();
    }
}