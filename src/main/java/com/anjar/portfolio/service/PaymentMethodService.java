package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.PaymentMethodDTO;
import com.anjar.portfolio.dto.PaymentMethodRequest;
import com.anjar.portfolio.entity.PaymentMethod;
import com.anjar.portfolio.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository repo;

    // ============================================================
    // PUBLIC — untuk halaman register
    // ============================================================
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getActiveMethods() {
        return repo.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // ADMIN — list semua
    // ============================================================
    @Transactional(readOnly = true)
    public List<PaymentMethodDTO> getAllMethods() {
        return repo.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ============================================================
    // ADMIN — get by id
    // ============================================================
    @Transactional(readOnly = true)
    public PaymentMethodDTO getById(Long id) {
        PaymentMethod m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method tidak ditemukan"));
        return toDTO(m);
    }

    // ============================================================
    // ADMIN — create
    // ============================================================
    @Transactional
    public PaymentMethodDTO create(PaymentMethodRequest req) {
        validate(req, null);

        PaymentMethod m = PaymentMethod.builder()
                .type(req.getType())
                .label(req.getLabel())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .qrisImageUrl(req.getQrisImageUrl())
                .qrisMerchantName(req.getQrisMerchantName())
                .bankName(req.getBankName())
                .bankAccountNumber(req.getBankAccountNumber())
                .bankAccountHolder(req.getBankAccountHolder())
                .ewalletProvider(req.getEwalletProvider())
                .ewalletAccount(req.getEwalletAccount())
                .ewalletAccountHolder(req.getEwalletAccountHolder())
                .cryptoChain(req.getCryptoChain())
                .cryptoToken(req.getCryptoToken())
                .cryptoAddress(req.getCryptoAddress())
                .cryptoNetworkNote(req.getCryptoNetworkNote())
                .instructions(req.getInstructions())
                .build();

        PaymentMethod saved = repo.save(m);
        log.info("✅ Payment method created: {} [{}]", saved.getLabel(), saved.getType());
        return toDTO(saved);
    }

    // ============================================================
    // ADMIN — update
    // ============================================================
    @Transactional
    public PaymentMethodDTO update(Long id, PaymentMethodRequest req) {
        PaymentMethod m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method tidak ditemukan"));

        validate(req, id);

        m.setType(req.getType());
        m.setLabel(req.getLabel());
        if (req.getIsActive() != null) m.setIsActive(req.getIsActive());
        if (req.getSortOrder() != null) m.setSortOrder(req.getSortOrder());

        // QRIS
        m.setQrisImageUrl(req.getQrisImageUrl());
        m.setQrisMerchantName(req.getQrisMerchantName());

        // Bank
        m.setBankName(req.getBankName());
        m.setBankAccountNumber(req.getBankAccountNumber());
        m.setBankAccountHolder(req.getBankAccountHolder());

        // EWALLET
        m.setEwalletProvider(req.getEwalletProvider());
        m.setEwalletAccount(req.getEwalletAccount());
        m.setEwalletAccountHolder(req.getEwalletAccountHolder());

        // Crypto
        m.setCryptoChain(req.getCryptoChain());
        m.setCryptoToken(req.getCryptoToken());
        m.setCryptoAddress(req.getCryptoAddress());
        m.setCryptoNetworkNote(req.getCryptoNetworkNote());

        // Common
        m.setInstructions(req.getInstructions());

        PaymentMethod saved = repo.save(m);
        log.info("✅ Payment method updated: {}", saved.getLabel());
        return toDTO(saved);
    }

    // ============================================================
    // ADMIN — toggle active
    // ============================================================
    @Transactional
    public PaymentMethodDTO toggleActive(Long id) {
        PaymentMethod m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method tidak ditemukan"));
        m.setIsActive(!Boolean.TRUE.equals(m.getIsActive()));
        return toDTO(repo.save(m));
    }

    // ============================================================
    // ADMIN — delete
    // ============================================================
    @Transactional
    public void delete(Long id) {
        PaymentMethod m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method tidak ditemukan"));

        // Cegah hapus kalau masih ada transaksi? → biarkan ON DELETE SET NULL
        repo.delete(m);
        log.info("🗑️ Payment method deleted: {}", m.getLabel());
    }

    // ============================================================
    // VALIDASI per type
    // ============================================================
    private void validate(PaymentMethodRequest req, Long excludeId) {
        if (req.getLabel() == null || req.getLabel().isBlank()) {
            throw new IllegalArgumentException("Label wajib diisi");
        }

        // Cek duplikat label + type
        if (repo.existsByLabelAndType(req.getLabel(), req.getType())) {
            // Kalau update, cek apakah yang duplikat itu dirinya sendiri
            if (excludeId == null) {
                throw new IllegalArgumentException(
                        "Label '" + req.getLabel() + "' sudah dipakai untuk type " + req.getType()
                );
            }
        }

        switch (req.getType()) {
            case QRIS -> {
                if (req.getQrisImageUrl() == null || req.getQrisImageUrl().isBlank()) {
                    throw new IllegalArgumentException("QRIS butuh gambar QR");
                }
            }
            case BANK_TRANSFER -> {
                if (req.getBankName() == null || req.getBankName().isBlank())
                    throw new IllegalArgumentException("Bank name wajib diisi");
                if (req.getBankAccountNumber() == null || req.getBankAccountNumber().isBlank())
                    throw new IllegalArgumentException("No rekening wajib diisi");
                if (req.getBankAccountHolder() == null || req.getBankAccountHolder().isBlank())
                    throw new IllegalArgumentException("Nama pemilik rekening wajib diisi");
            }
            case EWALLET -> {
                if (req.getEwalletProvider() == null || req.getEwalletProvider().isBlank())
                    throw new IllegalArgumentException("Provider e-wallet wajib diisi (GOPAY, DANA, dll)");
                if (req.getEwalletAccount() == null || req.getEwalletAccount().isBlank())
                    throw new IllegalArgumentException("Nomor akun e-wallet wajib diisi");
                if (req.getEwalletAccountHolder() == null || req.getEwalletAccountHolder().isBlank())
                    throw new IllegalArgumentException("Nama pemilik akun wajib diisi");
            }
            case CRYPTO_EVM -> {
                if (req.getCryptoChain() == null || req.getCryptoChain().isBlank())
                    throw new IllegalArgumentException("Chain wajib diisi (ethereum, polygon, dll)");
                if (req.getCryptoToken() == null || req.getCryptoToken().isBlank())
                    throw new IllegalArgumentException("Token wajib diisi (ETH, USDT, dll)");
                if (req.getCryptoAddress() == null || req.getCryptoAddress().isBlank())
                    throw new IllegalArgumentException("Address wallet wajib diisi");

                String addr = req.getCryptoAddress().trim();
                if (!addr.matches("^0x[a-fA-F0-9]{40}$")) {
                    throw new IllegalArgumentException("Format address EVM tidak valid (harus 0x + 40 hex)");
                }
            }
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private PaymentMethodDTO toDTO(PaymentMethod m) {
        return PaymentMethodDTO.builder()
                .id(m.getId())
                .type(m.getType())
                .label(m.getLabel())
                .isActive(m.getIsActive())
                .sortOrder(m.getSortOrder())
                .qrisImageUrl(m.getQrisImageUrl())
                .qrisMerchantName(m.getQrisMerchantName())
                .bankName(m.getBankName())
                .bankAccountNumber(m.getBankAccountNumber())
                .bankAccountHolder(m.getBankAccountHolder())
                .ewalletProvider(m.getEwalletProvider())
                .ewalletAccount(m.getEwalletAccount())
                .ewalletAccountHolder(m.getEwalletAccountHolder())
                .cryptoChain(m.getCryptoChain())
                .cryptoToken(m.getCryptoToken())
                .cryptoAddress(m.getCryptoAddress())
                .cryptoNetworkNote(m.getCryptoNetworkNote())
                .instructions(m.getInstructions())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}