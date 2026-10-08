package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.service.MessageService;
import com.anjar.portfolio.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class MessageController {

    private final MessageService messageService;

    // ===== PUBLIC — CONTACT FORM =====
    @PostMapping("/public/{portfolioSlug}")
    public ResponseEntity<MessageDTO> create(
            @PathVariable String portfolioSlug,
            @Valid @RequestBody MessageDTO dto) {
        dto.setRead(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(portfolioSlug, dto));
    }

    // ===== OWNER — INBOX =====
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAll() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getAll(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnread() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getUnread(userId));
    }

    @GetMapping("/count-unread")
    public ResponseEntity<Map<String, Long>> countUnread() {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getById(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.getById(id, userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean read) {
        Long userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(messageService.markAsRead(id, userId, read));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.requireCurrentUserId();
        messageService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}