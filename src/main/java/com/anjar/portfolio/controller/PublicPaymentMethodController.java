package com.anjar.portfolio.controller;

import com.anjar.portfolio.dto.PaymentMethodDTO;
import com.anjar.portfolio.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class PublicPaymentMethodController {

    private final PaymentMethodService service;

    /**
     * Endpoint publik — dipakai halaman register untuk list metode bayar.
     * Hanya return yang is_active = true.
     */
    @GetMapping
    public ResponseEntity<List<PaymentMethodDTO>> getActive() {
        return ResponseEntity.ok(service.getActiveMethods());
    }
}