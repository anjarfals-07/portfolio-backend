package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.entity.Message;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    // ===== GET ALL =====
    @Transactional(readOnly = true)
    public List<MessageDTO> getAll() {
        return messageRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ===== GET UNREAD =====
    @Transactional(readOnly = true)
    public List<MessageDTO> getUnread() {
        return messageRepository.findByReadOrderByCreatedAtDesc(false)
                .stream().map(this::toDTO).toList();
    }

    // ===== COUNT UNREAD =====
    @Transactional(readOnly = true)
    public long countUnread() {
        return messageRepository.countByReadFalse();
    }

    // ===== GET BY ID =====
    // Auto-mark as read saat dibuka
    @Transactional
    public MessageDTO getById(Long id) {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", id));

        // Auto mark as read
        if (Boolean.FALSE.equals(m.getRead())) {
            m.setRead(true);
            messageRepository.save(m);
        }

        return toDTO(m);
    }

    // ===== CREATE (dari contact form, public) =====
    @Transactional
    public MessageDTO create(MessageDTO dto) {
        Message m = Message.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .read(false)
                .build();
        return toDTO(messageRepository.save(m));
    }

    // ===== MARK AS READ =====
    @Transactional
    public MessageDTO markAsRead(Long id, boolean read) {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", id));
        m.setRead(read);
        return toDTO(messageRepository.save(m));
    }

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Message", id);
        }
        messageRepository.deleteById(id);
    }

    // ===== Mapper =====
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