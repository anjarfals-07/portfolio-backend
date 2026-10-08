package com.anjar.portfolio.controller;

import com.anjar.portfolio.service.CvGenerationService;
import com.anjar.portfolio.service.CvPreferences;
import com.anjar.portfolio.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/me/cv")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CvController {

    private final CvGenerationService cvGenerationService;

    // ============================================================
    // GENERATE CV (PDF)
    // ============================================================
    @PostMapping("/generate")
    public ResponseEntity<?> generateCv(
            @RequestParam(value = "template", required = false) String template) {
        try {
            Long userId = SecurityUtil.requireCurrentUserId();
            String username = SecurityUtil.getCurrentUsernameOrNull();

            log.info("🎨 Generate CV: user={} (id={}), template override={}",
                    username, userId, template);

            Map<String, Object> result = cvGenerationService.generateCv(userId, template);
            return ResponseEntity.ok(result);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));

        } catch (IOException e) {
            log.error("❌ IO error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Gagal simpan CV", "detail", e.getMessage()));

        } catch (RuntimeException e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null
                            ? e.getMessage() : "Gagal generate CV"));
        }
    }

    // ============================================================
    // ⭐ PREVIEW CV (HTML, bukan PDF)
    // ============================================================
    /**
     * Render HTML CV untuk iframe preview di frontend.
     * Return HTML mentah (bukan JSON).
     */
    @GetMapping(value = "/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(
            @RequestParam(value = "template", required = false) String template) {
        try {
            Long userId = SecurityUtil.requireCurrentUserId();

            log.info("👁️ Preview CV: user id={}, template={}", userId, template);

            String html = cvGenerationService.generatePreviewHtml(userId, template);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .body(html);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h1>401 Unauthorized</h1></body></html>");

        } catch (Exception e) {
            log.error("❌ Preview error: {}", e.getMessage(), e);
            String msg = e.getMessage() != null ? e.getMessage() : "Unknown error";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body style='font-family:sans-serif;padding:2rem;'>"
                            + "<h2>Gagal render preview</h2>"
                            + "<p style='color:#666;'>" + msg + "</p>"
                            + "</body></html>");
        }
    }

    // ============================================================
    // OPTIONS — pilihan valid
    // ============================================================
    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getOptions() {
        return ResponseEntity.ok(Map.of(
                "template", List.of(
                        Map.of("id", "modern",  "label", "Modern",  "desc", "Sidebar berwarna, bold"),
                        Map.of("id", "classic", "label", "Classic", "desc", "Formal, ATS-friendly"),
                        Map.of("id", "minimal", "label", "Minimal", "desc", "Clean, banyak whitespace")
                ),
                "layouts", List.of("sidebar-left", "sidebar-right", "header-top", "two-col", "timeline"),
                "palettes", CvPreferences.PALETTES.keySet(),
                "fontPairs", CvPreferences.FONT_PAIRS.keySet(),
                "themes", List.of("light", "dark"),
                "densities", List.of("compact", "normal", "spacious"),
                "cardStyles", List.of("soft", "flat", "outline", "glass", "none"),
                "badgeStyles", List.of("pill", "square", "outline", "minimal"),
                "iconSets", List.of("primeicons", "lucide", "none"),
                "backgroundPatterns", List.of("none", "dots", "lines", "mesh")
        ));
    }

    // ============================================================
    // STATUS
    // ============================================================
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCvStatus() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Gunakan ProfileService.getProfile() untuk cvUrl & cvSource"
        ));
    }
}