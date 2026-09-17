package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.service.MessageService;
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

    // ===== CREATE (PUBLIC — dari contact form) =====
    // POST /api/messages
    @PostMapping
    public ResponseEntity<MessageDTO> create(@Valid @RequestBody MessageDTO dto) {
        // Force read = false saat create
        dto.setRead(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(dto));
    }

    // ===== GET ALL (ADMIN) =====
    // GET /api/messages
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAll() {
        return ResponseEntity.ok(messageService.getAll());
    }

    // ===== GET UNREAD (ADMIN) =====
    // GET /api/messages/unread
    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnread() {
        return ResponseEntity.ok(messageService.getUnread());
    }

    // ===== COUNT UNREAD (ADMIN) =====
    // GET /api/messages/count-unread
    @GetMapping("/count-unread")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread()));
    }

    // ===== GET BY ID (ADMIN — auto mark as read) =====
    // GET /api/messages/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getById(id));
    }

    // ===== MARK AS READ / UNREAD (ADMIN) =====
    // PATCH /api/messages/{id}/read?read=true
    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean read) {
        return ResponseEntity.ok(messageService.markAsRead(id, read));
    }

    // ===== DELETE (ADMIN) =====
    // DELETE /api/messages/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}