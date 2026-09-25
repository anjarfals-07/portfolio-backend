package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.*;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.entity.UserRole;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // ============================================================
    // PUBLIC
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserPublicDTO> getAllPublicUsers() {
        return userRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                .stream().map(this::toPublicDTO).toList();
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
    // ADMIN
    // ============================================================

    @Transactional(readOnly = true)
    public List<UserAdminDTO> getAllForAdmin() {
        return userRepository.findAll().stream()
                .map(this::toAdminDTO).toList();
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
                .active(true)
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

        try {
            user.setRole(UserRole.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role tidak valid: " + role);
        }

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
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}