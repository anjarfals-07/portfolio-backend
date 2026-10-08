package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.entity.Message;
import com.anjar.portfolio.entity.Profile;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.MessageRepository;
import com.anjar.portfolio.repository.ProfileRepository;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;   // ← TAMBAH (Fase 9.15)

    // ============================================================
    // OWNER — INBOX
    // ============================================================

    @Transactional(readOnly = true)
    public List<MessageDTO> getAll(Long userId) {
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getUnread(Long userId) {
        return messageRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return messageRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public MessageDTO getById(Long id, Long userId) {
        Message m = findOwned(id, userId);
        if (Boolean.FALSE.equals(m.getRead())) {
            m.setRead(true);
            messageRepository.save(m);
        }
        return toDTO(m);
    }

    @Transactional
    public MessageDTO markAsRead(Long id, Long userId, boolean read) {
        Message m = findOwned(id, userId);
        m.setRead(read);
        return toDTO(messageRepository.save(m));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Message m = findOwned(id, userId);
        messageRepository.delete(m);
    }

    // ============================================================
    // PUBLIC — CONTACT FORM (guest)
    // ============================================================

    /**
     * Guest submit contact form ke portfolio user.
     *
     * Flow:
     * 1. Cari user (owner portfolio) by portfolioSlug
     * 2. Simpan message ke DB
     * 3. Kirim email notif ke owner
     */
    @Transactional
    public MessageDTO create(String portfolioSlug, MessageDTO dto) {
        // 1. Cari owner
        User recipient = userRepository.findByPortfolioSlug(portfolioSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "slug", portfolioSlug
                ));

        // 2. Simpan message
        Message m = Message.builder()
                .user(recipient)
                .name(dto.getName())
                .email(dto.getEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .read(false)
                .build();

        Message saved = messageRepository.save(m);
        log.info("📩 New message from {} to {} (slug={})",
                dto.getEmail(), recipient.getUsername(), portfolioSlug);

        // 3. KIRIM EMAIL NOTIF KE OWNER (Fase 9.15)
        try {
            sendNewMessageNotifToOwner(saved, recipient);
        } catch (Exception e) {
            log.error("⚠️ Failed to send message notif email: {}", e.getMessage());
            // Gak throw — biar submit contact form tetap sukses walaupun email gagal
        }

        return toDTO(saved);
    }

    // ============================================================
    // HELPER — EMAIL
    // ============================================================

    /**
     * Kirim email notifikasi ke owner saat ada pesan baru.
     */
    private void sendNewMessageNotifToOwner(Message message, User owner) {
        // Ambil display name dari profile (kalau ada)
        Profile profile = profileRepository.findByUserId(owner.getId()).orElse(null);

        String ownerName = profile != null && profile.getFullName() != null
                ? profile.getFullName()
                : (owner.getDisplayName() != null
                ? owner.getDisplayName()
                : owner.getUsername());

        // Build variables untuk template
        Map<String, String> vars = Map.of(
                "ownerName", ownerName,
                "senderName", message.getName(),
                "senderEmail", message.getEmail(),
                "subject", message.getSubject() != null && !message.getSubject().isBlank()
                        ? message.getSubject()
                        : "(Tanpa Subjek)",
                "message", message.getMessage(),
                "inboxUrl", buildInboxUrl(owner)
        );

        // Kirim email
        emailService.sendNewMessageEmail(
                owner.getEmail(),
                "📬 Pesan Baru: " + (message.getSubject() != null ? message.getSubject() : "Tanpa Subjek"),
                "new-message.html",
                vars
        );

        log.info("✅ New message notif sent to {}", owner.getEmail());
    }

    /**
     * Build URL inbox owner.
     */
    private String buildInboxUrl(User owner) {
        // Ambil frontend URL dari EmailService (via getter) — atau hardcode
        // Nanti bisa di-inject via @Value("${app.frontend-url}")
        return "http://localhost:5173/" + owner.getPortfolioSlug() + "/dashboard/inbox";
    }

    // ============================================================
    // HELPER — OWNERSHIP
    // ============================================================

    private Message findOwned(Long id, Long userId) {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", id));
        if (!m.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Message ini bukan milik kamu");
        }
        return m;
    }

    // ============================================================
    // Mapper
    // ============================================================

    private MessageDTO toDTO(Message m) {
        return MessageDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .email(m.getEmail())
                .subject(m.getSubject())
                .message(m.getMessage())
                .read(m.getRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}