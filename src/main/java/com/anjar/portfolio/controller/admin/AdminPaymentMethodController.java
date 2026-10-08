package com.anjar.portfolio.controller.admin;

import com.anjar.portfolio.dto.PaymentMethodDTO;
import com.anjar.portfolio.dto.PaymentMethodRequest;
import com.anjar.portfolio.service.FileStorageService;
import com.anjar.portfolio.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/payment-methods")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminPaymentMethodController {

    private final PaymentMethodService service;
    private final FileStorageService fileStorage;

    // ============================================================
    // LIST
    // ============================================================
    @GetMapping
    public ResponseEntity<List<PaymentMethodDTO>> getAll() {
        return ResponseEntity.ok(service.getAllMethods());
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    public ResponseEntity<PaymentMethodDTO> create(
            @Valid @RequestBody PaymentMethodRequest req
    ) {
        return ResponseEntity.ok(service.create(req));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentMethodRequest req
    ) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // ============================================================
    // TOGGLE ACTIVE
    // ============================================================
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<PaymentMethodDTO> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleActive(id));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // UPLOAD QRIS IMAGE
    // ============================================================
    @PostMapping("/upload-qris")
    public ResponseEntity<Map<String, String>> uploadQris(
            @RequestParam("file") MultipartFile file
    ) {
        String url = fileStorage.upload(file, "qris");
        return ResponseEntity.ok(Map.of("url", url));
    }
}