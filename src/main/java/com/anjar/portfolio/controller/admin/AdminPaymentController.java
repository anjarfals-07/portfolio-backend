package com.anjar.portfolio.controller.admin;

import com.anjar.portfolio.dto.PaymentAdminDTO;
import com.anjar.portfolio.dto.PaymentStatsDTO;
import com.anjar.portfolio.dto.PaymentVerificationRequest;
import com.anjar.portfolio.util.SecurityUtil;
import com.anjar.portfolio.service.PaymentVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class AdminPaymentController {

    private final PaymentVerificationService service;

    @GetMapping
    public ResponseEntity<Page<PaymentAdminDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.listPayments(status, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDTO> stats() {
        return ResponseEntity.ok(service.getStats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentAdminDTO> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<PaymentAdminDTO> approve(@PathVariable Long id) {
        Long adminId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(service.approve(id, adminId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PaymentAdminDTO> reject(
            @PathVariable Long id,
            @Valid @RequestBody PaymentVerificationRequest req
    ) {
        Long adminId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(
                service.reject(id, adminId, req.getRejectionReason())
        );
    }
}