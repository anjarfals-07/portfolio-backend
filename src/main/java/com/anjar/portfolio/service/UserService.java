package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserRole;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final BlogPostRepository blogPostRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final TechStackRepository techStackRepository;
    private final MessageRepository messageRepository;
    private final SlugService slugService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;   // ← TAMBAH (Fase 9.13)

    // ============================================================
    // PUBLIC
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserPublicDTO> getAllPublicUsers() {
        return userRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(this::toPublicDTO)
                .toList();
    }

    // ============================================================
    // ME
    // ============================================================

    @Transactional(readOnly = true)
    public UserMeDTO getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return UserMeDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .portfolioSlug(user.getPortfolioSlug())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ============================================================
    // ADMIN — USER CRUD
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllForAdmin() {
        return userRepository.findAll().stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAdminDTO getByIdForAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toAdminDTO(user);
    }

    @Transactional
    public UserAdminDTO createByAdmin(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username sudah dipakai");
        }

        String slug;
        if (req.getPortfolioSlug() != null && !req.getPortfolioSlug().isBlank()) {
            slugService.validateSlugForNewUser(req.getPortfolioSlug());
            slug = req.getPortfolioSlug();
        } else {
            slug = slugService.generateUniqueSlug(req.getUsername(), true);
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(req.getEmail())
                .displayName(req.getDisplayName() != null ? req.getDisplayName() : req.getUsername())
                .portfolioSlug(slug)
                .role(req.getRole() != null ? UserRole.valueOf(req.getRole()) : UserRole.OWNER)
                .status(UserStatus.ACTIVE)
                .active(true)
                .approvedAt(LocalDateTime.now())
                .build();

        return toAdminDTO(userRepository.save(user));
    }

    @Transactional
    public UserAdminDTO updateByAdmin(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());

        if (req.getPortfolioSlug() != null && !req.getPortfolioSlug().isBlank()) {
            slugService.validateSlugForUser(req.getPortfolioSlug(), id);
            user.setPortfolioSlug(req.getPortfolioSlug());
        }

        if (req.getRole() != null) user.setRole(UserRole.valueOf(req.getRole()));
        if (req.getActive() != null) user.setActive(req.getActive());

        return toAdminDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }

    @Transactional
    public UserAdminDTO setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(active);
        return toAdminDTO(userRepository.save(user));
    }

    @Transactional
    public UserAdminDTO changeRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setRole(UserRole.valueOf(role));
        return toAdminDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        return AdminStatsDTO.builder()
                .totalUsers(userRepository.count())
                .totalOwners(userRepository.countByRole(UserRole.OWNER))
                .totalSuperAdmins(userRepository.countByRole(UserRole.SUPER_ADMIN))
                .activeUsers(userRepository.countByActiveTrue())
                .totalProjects(projectRepository.count())
                .totalBlogPosts(blogPostRepository.count())
                .totalSkills(skillRepository.count())
                .totalExperiences(experienceRepository.count())
                .totalTechStacks(techStackRepository.count())
                .totalMessages(messageRepository.count())
                .build();
    }

    // ============================================================
    // APPROVAL
    // ============================================================

    /**
     * Approve user + kirim email notif.
     */
    @Transactional
    public UserAdminDTO approveUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(adminId);
        user.setRejectionReason(null);

        User saved = userRepository.save(user);
        log.info("✅ User approved: {} (id={}) by admin {}",
                saved.getUsername(), saved.getId(), adminId);

        // ===== KIRIM EMAIL (Fase 9.13) =====
        try {
            emailService.sendApprovedEmail(
                    saved.getEmail(),
                    saved.getUsername(),
                    saved.getPortfolioSlug()
            );
        } catch (Exception e) {
            log.error("⚠️ Failed to send approved email: {}", e.getMessage());
        }

        return toAdminDTO(saved);
    }

    /**
     * Reject user + kirim email notif.
     */
    @Transactional
    public UserAdminDTO rejectUser(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setStatus(UserStatus.REJECTED);
        user.setRejectionReason(reason);
        user.setApprovedBy(adminId);

        User saved = userRepository.save(user);
        log.warn("❌ User rejected: {} (id={}) by admin {} (reason: {})",
                saved.getUsername(), saved.getId(), adminId, reason);

        // ===== KIRIM EMAIL (Fase 9.13) =====
        try {
            emailService.sendRejectedEmail(
                    saved.getEmail(),
                    saved.getUsername(),
                    reason
            );
        } catch (Exception e) {
            log.error("⚠️ Failed to send rejected email: {}", e.getMessage());
        }

        return toAdminDTO(saved);
    }

    /**
     * Suspend user — gak kirim email (opsional).
     */
    @Transactional
    public UserAdminDTO suspendUser(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setStatus(UserStatus.SUSPENDED);
        user.setRejectionReason(reason);
        user.setApprovedBy(adminId);

        User saved = userRepository.save(user);
        log.warn("⚠️ User suspended: {} (id={}) by admin {} (reason: {})",
                saved.getUsername(), saved.getId(), adminId, reason);

        // TODO: kirim email suspend (opsional — bisa dibuat nanti)
        // emailService.sendSuspendedEmail(...)

        return toAdminDTO(saved);
    }

    // ============================================================
    // Mapper
    // ============================================================

    private UserPublicDTO toPublicDTO(User u) {
        return UserPublicDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .displayName(u.getDisplayName())
                .portfolioSlug(u.getPortfolioSlug())
                .role(u.getRole().name())
                .createdAt(u.getCreatedAt())
                .build();
    }

    private UserAdminDTO toAdminDTO(User u) {
        return UserAdminDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .displayName(u.getDisplayName())
                .portfolioSlug(u.getPortfolioSlug())
                .role(u.getRole().name())
                .status(u.getStatus().name())
                .active(u.getActive())
                .rejectionReason(u.getRejectionReason())
                .approvedAt(u.getApprovedAt())
                .approvedBy(u.getApprovedBy())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}