package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.PaymentInitResponse;
import com.anjar.portfolio.dto.PaymentStatusResponse;
import com.anjar.portfolio.dto.PaymentTransactionDTO;
import com.anjar.portfolio.entity.PaymentMethod;
import com.anjar.portfolio.entity.PaymentTransaction;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.PaymentStatus;
import com.anjar.portfolio.repository.PaymentMethodRepository;
import com.anjar.portfolio.repository.PaymentTransactionRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String REF_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final PaymentTransactionRepository txRepo;
    private final PaymentMethodRepository methodRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLog;
    private final EmailService emailService;

    // ============================================================
    // CREATE PAYMENT
    // ============================================================
    @Transactional
    public PaymentInitResponse createPayment(
            Long userId,
            BigDecimal amount,
            String purpose,
            Long paymentMethodId,
            int expiryMinutes
    ) {
        PaymentMethod method = methodRepo.findById(paymentMethodId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment method tidak ditemukan"
                ));

        if (!Boolean.TRUE.equals(method.getIsActive())) {
            throw new IllegalArgumentException(
                    "Payment method tidak aktif. Pilih yang lain."
            );
        }

        if (txRepo.hasActiveTransaction(userId)) {
            throw new IllegalStateException(
                    "Kamu masih punya transaksi aktif. Selesaikan dulu."
            );
        }

        String referenceId = generateReferenceId();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        PaymentTransaction tx = PaymentTransaction.builder()
                .userId(userId)
                .referenceId(referenceId)
                .paymentMethodId(method.getId())
                .methodType(method.getType())
                .methodLabel(method.getLabel())
                .amountIdr(amount)
                .status(PaymentStatus.PENDING)
                .proofUploadCount(0)
                .expiredAt(expiredAt)
                .build();

        PaymentTransaction saved = txRepo.save(tx);

        log.info("💳 Payment created: ref={}, user={}, amount={}, method={}, purpose={}, expires={}",
                referenceId, userId, amount, method.getLabel(), purpose, expiredAt);

        User user = userRepo.findById(userId).orElse(null);
        if (user != null) {
            activityLog.logPaymentCreated(user, saved.getId(), referenceId);
        }

        return toInitResponse(saved, method);
    }

    // ============================================================
    // GET BY REFERENCE
    // ============================================================
    @Transactional(readOnly = true)
    public PaymentInitResponse getByReference(String referenceId) {
        PaymentTransaction tx = txRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));

        PaymentMethod method = tx.getPaymentMethodId() != null
                ? methodRepo.findById(tx.getPaymentMethodId()).orElse(null)
                : null;

        return toInitResponse(tx, method);
    }

    // ============================================================
    // UPLOAD PROOF
    // ============================================================
    @Transactional
    public PaymentTransactionDTO uploadProof(
            String referenceId,
            String proofImageUrl,
            String proofNote
    ) {
        PaymentTransaction tx = txRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));

        if (!tx.canUploadProof()) {
            if (tx.getStatus() == PaymentStatus.PAID) {
                throw new IllegalStateException("Pembayaran sudah dikonfirmasi");
            }
            if (tx.getStatus() == PaymentStatus.WAITING_VERIFICATION) {
                throw new IllegalStateException(
                        "Bukti sudah diupload. Tunggu verifikasi admin."
                );
            }
            if (tx.isExpired()) {
                throw new IllegalStateException("Transaksi sudah expired");
            }
            if (tx.getProofUploadCount() != null && tx.getProofUploadCount() >= 3) {
                throw new IllegalStateException(
                        "Maksimal upload bukti 3x. Hubungi admin."
                );
            }
            throw new IllegalStateException("Tidak bisa upload bukti sekarang");
        }

        tx.setProofImageUrl(proofImageUrl);
        tx.setProofNote(proofNote);
        tx.setProofUploadedAt(LocalDateTime.now());
        tx.setProofUploadCount(tx.getProofUploadCount() + 1);

        if (tx.getStatus() == PaymentStatus.PENDING
                || tx.getStatus() == PaymentStatus.REJECTED) {
            tx.setStatus(PaymentStatus.WAITING_VERIFICATION);
            tx.setRejectionReason(null);
        }

        PaymentTransaction saved = txRepo.save(tx);

        log.info("📎 Proof uploaded: ref={}, upload#{}",
                referenceId, saved.getProofUploadCount());

        // ⭐ Notifikasi admin
        try {
            User user = userRepo.findById(saved.getUserId()).orElse(null);
            String username = user != null ? user.getUsername() : "unknown";
            emailService.sendAdminPaymentNotification(
                    referenceId,
                    username,
                    saved.getMethodLabel()
            );
        } catch (Exception e) {
            log.warn("⚠️ Failed to send admin notification: {}", e.getMessage());
        }

        return toDTO(saved);
    }

    // ============================================================
    // GET STATUS (public — polling)
    // ============================================================
    @Transactional(readOnly = true)
    public PaymentStatusResponse getStatus(String referenceId) {
        PaymentTransaction tx = txRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));

        return PaymentStatusResponse.builder()
                .referenceId(tx.getReferenceId())
                .status(tx.getStatus().name())
                .methodLabel(tx.getMethodLabel())
                .amountIdr(tx.getAmountIdr())
                .proofImageUrl(tx.getProofImageUrl())
                .proofUploadedAt(tx.getProofUploadedAt())
                .rejectionReason(tx.getRejectionReason())
                .expiredAt(tx.getExpiredAt())
                .paidAt(tx.getPaidAt())
                .build();
    }

    // ============================================================
    // MAPPER — to DTO
    // ============================================================
    private PaymentTransactionDTO toDTO(PaymentTransaction tx) {
        return PaymentTransactionDTO.builder()
                .id(tx.getId())
                .userId(tx.getUserId())
                .referenceId(tx.getReferenceId())
                .paymentMethodId(tx.getPaymentMethodId())
                .methodType(tx.getMethodType().name())
                .methodLabel(tx.getMethodLabel())
                .amountIdr(tx.getAmountIdr())
                .status(tx.getStatus().name())
                .proofImageUrl(tx.getProofImageUrl())
                .proofNote(tx.getProofNote())
                .proofUploadedAt(tx.getProofUploadedAt())
                .proofUploadCount(tx.getProofUploadCount())
                .rejectionReason(tx.getRejectionReason())
                .verifiedAt(tx.getVerifiedAt())
                .expiredAt(tx.getExpiredAt())
                .paidAt(tx.getPaidAt())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }

    // ============================================================
    // GENERATE REFERENCE ID
    // ============================================================
    private String generateReferenceId() {
        String datePart = LocalDateTime.now().format(DATE_FMT);

        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder("PAY-")
                    .append(datePart)
                    .append("-");

            for (int i = 0; i < 6; i++) {
                sb.append(REF_CHARS.charAt(RANDOM.nextInt(REF_CHARS.length())));
            }

            String ref = sb.toString();
            if (!txRepo.existsByReferenceId(ref)) {
                return ref;
            }
        }

        throw new IllegalStateException("Gagal generate reference ID, coba lagi");
    }

    // ============================================================
    // MAPPER — to Init Response
    // ============================================================
    private PaymentInitResponse toInitResponse(
            PaymentTransaction tx,
            PaymentMethod method
    ) {
        PaymentInitResponse.PaymentInitResponseBuilder builder =
                PaymentInitResponse.builder()
                        .referenceId(tx.getReferenceId())
                        .amountIdr(tx.getAmountIdr())
                        .provider("MANUAL")
                        .method(tx.getMethodType().name())
                        .methodLabel(tx.getMethodLabel())
                        .status(tx.getStatus().name())
                        .expiredAt(tx.getExpiredAt());

        if (method != null) {
            builder.qrisImageUrl(method.getQrisImageUrl())
                    .qrisMerchantName(method.getQrisMerchantName())
                    .bankName(method.getBankName())
                    .bankAccountNumber(method.getBankAccountNumber())
                    .bankAccountHolder(method.getBankAccountHolder())
                    .cryptoChain(method.getCryptoChain())
                    .cryptoToken(method.getCryptoToken())
                    .cryptoAddress(method.getCryptoAddress())
                    .cryptoNetworkNote(method.getCryptoNetworkNote())
                    // ⭐ EWALLET
                    .ewalletProvider(method.getEwalletProvider())
                    .ewalletAccount(method.getEwalletAccount())
                    .ewalletAccountHolder(method.getEwalletAccountHolder())
                    .instructions(method.getInstructions());
        }

        return builder.build();
    }
}