package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.entity.Message;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.ForbiddenException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.MessageRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

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

    // ===== CREATE — dipanggil dari public contact form =====
    @Transactional
    public MessageDTO create(String portfolioSlug, MessageDTO dto) {
        User recipient = userRepository.findByPortfolioSlug(portfolioSlug)
                .orElseThrow(() -> new ResourceNotFoundException("User", "slug", portfolioSlug));

        Message m = Message.builder()
                .user(recipient)
                .name(dto.getName())
                .email(dto.getEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .read(false)
                .build();
        return toDTO(messageRepository.save(m));
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

    private Message findOwned(Long id, Long userId) {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", id));
        if (!m.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Message ini bukan milik kamu");
        }
        return m;
    }

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