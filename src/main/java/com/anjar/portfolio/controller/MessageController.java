package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.MessageDTO;
import com.anjar.portfolio.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class MessageController {

    private final MessageService messageService;

    // Simple rate limiter: max 3 request per IP per jam
    private final Map<String, RateLimitData> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 3;
    private static final int WINDOW_HOURS = 1;

    // ===== CREATE (PUBLIC — dari contact form) =====
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody MessageDTO dto,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            HttpServletRequest request
    ) {
        String clientIp = extractClientIp(forwardedFor, request);

        if (!checkRateLimit(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Map.of("message", "Terlalu banyak pesan. Coba lagi dalam 1 jam.")
            );
        }

        dto.setRead(false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(dto));
    }

    // ===== GET ALL (ADMIN) =====
    @GetMapping
    public ResponseEntity<List<MessageDTO>> getAll() {
        return ResponseEntity.ok(messageService.getAll());
    }

    // ===== GET UNREAD (ADMIN) =====
    @GetMapping("/unread")
    public ResponseEntity<List<MessageDTO>> getUnread() {
        return ResponseEntity.ok(messageService.getUnread());
    }

    // ===== COUNT UNREAD (ADMIN) =====
    @GetMapping("/count-unread")
    public ResponseEntity<Map<String, Long>> countUnread() {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread()));
    }

    // ===== GET BY ID (ADMIN — auto mark as read) =====
    @GetMapping("/{id}")
    public ResponseEntity<MessageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getById(id));
    }

    // ===== MARK AS READ / UNREAD (ADMIN) =====
    @PatchMapping("/{id}/read")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean read) {
        return ResponseEntity.ok(messageService.markAsRead(id, read));
    }

    // ===== DELETE (ADMIN) =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Helpers =====
    private String extractClientIp(String forwardedFor, HttpServletRequest request) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean checkRateLimit(String ip) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitData data = rateLimitMap.get(ip);

        if (data == null || data.windowStart.plusHours(WINDOW_HOURS).isBefore(now)) {
            rateLimitMap.put(ip, new RateLimitData(now, 1));
            return true;
        }

        if (data.count >= MAX_REQUESTS) {
            return false;
        }

        data.count++;
        return true;
    }

    private static class RateLimitData {
        LocalDateTime windowStart;
        int count;

        RateLimitData(LocalDateTime windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}