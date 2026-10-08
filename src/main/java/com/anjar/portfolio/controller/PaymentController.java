package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.PaymentInitResponse;
import com.anjar.portfolio.dto.PaymentStatusResponse;
import com.anjar.portfolio.dto.PaymentTransactionDTO;
import com.anjar.portfolio.service.FileStorageService;
import com.anjar.portfolio.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class PaymentController {

    private final PaymentService paymentService;
    private final FileStorageService fileStorage;

    // ============================================================
    // GET DETAIL TRANSAKSI (public — untuk halaman payment)
    // ============================================================
    @GetMapping("/{referenceId}")
    public ResponseEntity<PaymentInitResponse> getDetail(
            @PathVariable String referenceId
    ) {
        return ResponseEntity.ok(paymentService.getByReference(referenceId));
    }

    // ============================================================
    // GET STATUS (public — untuk polling)
    // ============================================================
    @GetMapping("/status/{referenceId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(
            @PathVariable String referenceId
    ) {
        return ResponseEntity.ok(paymentService.getStatus(referenceId));
    }

    // ============================================================
    // UPLOAD BUKTI BAYAR (public — user yang belum login)
    // ============================================================
    @PostMapping("/{referenceId}/proof")
    public ResponseEntity<PaymentTransactionDTO> uploadProof(
            @PathVariable String referenceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note
    ) {
        // Upload file dulu
        String proofUrl = fileStorage.upload(file, "proofs");

        // Lalu update transaksi
        PaymentTransactionDTO result = paymentService.uploadProof(
                referenceId,
                proofUrl,
                note
        );

        return ResponseEntity.ok(result);
    }
}