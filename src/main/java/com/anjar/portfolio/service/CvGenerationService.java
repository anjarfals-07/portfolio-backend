package com.anjar.portfolio.service;

import com.anjar.portfolio.entity.*;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.*;
import com.anjar.portfolio.util.CvFileUtil;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service generate CV PDF dari data portfolio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CvGenerationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillRepository skillRepository;
    private final TechStackRepository techStackRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final TemplateEngine templateEngine;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ============================================================
    // GENERATE CV — PDF
    // ============================================================
    @Transactional
    public Map<String, Object> generateCv(Long userId) throws IOException {
        return generateCv(userId, null);
    }

    @Transactional
    public Map<String, Object> generateCv(Long userId, String templateOverride)
            throws IOException {

        // ===== 1. Load user & profile =====
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup. Isi profile dulu sebelum generate CV."));

        // ===== 2. Parse preferences =====
        CvPreferences prefs = CvPreferences.parse(profile.getCvPreferences());

        if (templateOverride != null && !templateOverride.isBlank()) {
            String t = templateOverride.toLowerCase();
            if (CvPreferences.VALID_TEMPLATES.contains(t)) {
                prefs.template = t;
            } else {
                log.warn("⚠️ Invalid template override '{}', using '{}'",
                        t, prefs.template);
            }
        }

        log.info("🎨 Generating CV user={} (id={}) — template={}, layout={}, palette={}, theme={}",
                user.getUsername(), userId,
                prefs.template, prefs.layout, prefs.palette, prefs.theme);

        // ===== 3. Load data =====
        Map<String, Object> data = buildCvData(user, profile, prefs);

        // ===== 4. Render HTML =====
        String templatePath = "cv/" + prefs.template;
        Context context = new Context();
        context.setVariables(data);

        String html;
        try {
            html = templateEngine.process(templatePath, context);
            log.info("✅ Template rendered: {}", templatePath);
        } catch (Exception e) {
            log.error("❌ Failed to render '{}': {}", templatePath, e.getMessage());
            throw new RuntimeException(
                    "Gagal render template CV '" + prefs.template
                            + "'. Pastikan file template/cv/" + prefs.template + ".html ada.", e);
        }

        // ===== 5. Convert HTML → PDF =====
        byte[] pdfBytes;
        try {
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, baseUrl + "/");
            builder.toStream(pdfStream);
            builder.run();
            pdfBytes = pdfStream.toByteArray();
        } catch (Exception e) {
            log.error("❌ Failed HTML→PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal convert HTML ke PDF. Cek log.", e);
        }

        // ============================================================
        // 6. Save PDF
        // ⭐ Hapus SEMUA CV lama user (upload + generated)
        // ============================================================
        Path cvDir = Paths.get(uploadDir, "cv").toAbsolutePath().normalize();
        Files.createDirectories(cvDir);

        // Delete semua CV lama — biar cuma 1 CV aktif per user
        CvFileUtil.deleteAllByUser(userId, cvDir);

        String filename = CvFileUtil.buildGeneratedFilename(userId);
        Path targetPath = cvDir.resolve(filename);

        if (!targetPath.normalize().startsWith(cvDir)) {
            throw new SecurityException("Invalid target path");
        }

        Files.write(targetPath, pdfBytes);

        // ===== 7. Update profile =====
        String fileUrl = baseUrl + "/uploads/cv/" + filename;
        profile.setCvUrl(fileUrl);
        profile.setCvSource(Profile.CvSource.GENERATED);
        profile.setCvGeneratedAt(LocalDateTime.now());
        profileRepository.save(profile);

        log.info("✅ CV generated: {} ({} bytes, template={})",
                filename, pdfBytes.length, prefs.template);

        // ===== 8. Return =====
        Map<String, Object> result = new HashMap<>();
        result.put("url", fileUrl);
        result.put("publicId", filename);
        result.put("source", "GENERATED");
        result.put("template", prefs.template);
        result.put("layout", prefs.layout);
        result.put("theme", prefs.theme);
        result.put("generatedAt", profile.getCvGeneratedAt().toString());
        result.put("sizeBytes", pdfBytes.length);
        return result;
    }

    // ============================================================
    // ⭐ GENERATE PREVIEW — HTML (bukan PDF)
    // ============================================================
    @Transactional(readOnly = true)
    public String generatePreviewHtml(Long userId, String templateOverride) {

        // ===== 1. Load =====
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profile belum di-setup. Isi profile dulu."));

        // ===== 2. Parse preferences =====
        CvPreferences prefs = CvPreferences.parse(profile.getCvPreferences());

        if (templateOverride != null && !templateOverride.isBlank()) {
            String t = templateOverride.toLowerCase();
            if (CvPreferences.VALID_TEMPLATES.contains(t)) {
                prefs.template = t;
            }
        }

        // ===== 3. Build data =====
        Map<String, Object> data = buildCvData(user, profile, prefs);

        // ===== 4. Render =====
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process("cv/" + prefs.template, context);
    }

    // ============================================================
    // ⭐ HELPER: BUILD DATA (shared antara generateCv & preview)
    // ============================================================
    private Map<String, Object> buildCvData(User user, Profile profile, CvPreferences prefs) {

        // ===== Projects =====
        List<Project> projects = prefs.showProjects
                ? projectRepository.findByUserIdAndPublishedTrueOrderByCreatedAtDesc(user.getId())
                : List.of();

        // ===== Experiences (achievement lama) =====
        List<Experience> experiences = prefs.showExperiences
                ? experienceRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(user.getId())
                : List.of();

        // ===== Skills =====
        List<Skill> skills = prefs.showSkills
                ? skillRepository.findByUserIdOrderByCategoryAscSortOrderAsc(user.getId())
                : List.of();

        // ===== Tech Stack =====
        List<TechStack> techStacks = prefs.showTechStack
                ? techStackRepository.findByUserIdOrderBySortOrderAscNameAsc(user.getId())
                : List.of();

        // ⭐ Education
        List<Education> educations = prefs.showEducation
                ? educationRepository.findByUserIdOrderBySortOrderAscStartDateDesc(user.getId())
                : List.of();

        // ⭐ Work Experience
        List<WorkExperience> workExperiences = prefs.showWorkExperience
                ? workExperienceRepository.findByUserIdOrderBySortOrderAscStartDateDesc(user.getId())
                : List.of();

        // ===== Group skills by category =====
        Map<String, List<Skill>> skillsByCategory = skills.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() != null ? s.getCategory() : "Umum",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // ===== Build data =====
        Map<String, Object> data = new HashMap<>();
        data.put("profile", profile);
        data.put("user", user);
        data.put("projects", projects);
        data.put("experiences", experiences);
        data.put("skillsByCategory", skillsByCategory);
        data.put("techStacks", techStacks);
        data.put("educations", educations);
        data.put("workExperiences", workExperiences);
        data.put("generatedAt", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"))));
        data.put("baseUrl", baseUrl);
        data.put("portfolioUrl", baseUrl + "/" + user.getPortfolioSlug());
        data.putAll(prefs.toTemplateVars());

        return data;
    }
}