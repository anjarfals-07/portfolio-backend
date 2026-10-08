package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.PaymentAdminDTO;
import com.anjar.portfolio.dto.PaymentStatsDTO;
import com.anjar.portfolio.entity.PaymentMethod;
import com.anjar.portfolio.entity.PaymentTransaction;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.UserStatus;
import com.anjar.portfolio.enums.PaymentStatus;
import com.anjar.portfolio.enums.UserPaymentStatus;
import com.anjar.portfolio.event.PaymentRejectedEvent;
import com.anjar.portfolio.event.PaymentSuccessEvent;
import com.anjar.portfolio.repository.PaymentMethodRepository;
import com.anjar.portfolio.repository.PaymentTransactionRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationService {

    private final PaymentTransactionRepository txRepo;
    private final PaymentMethodRepository methodRepo;
    private final UserRepository userRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformSettingService settingService;
    private final ActivityLogService activityLog;   // ⭐ BARU

    // ============================================================
    // LIST
    // ============================================================
    @Transactional(readOnly = true)
    public Page<PaymentAdminDTO> listPayments(
            String statusFilter,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<PaymentTransaction> pageResult;

        if (statusFilter == null || statusFilter.isBlank() || statusFilter.equals("ALL")) {
            pageResult = txRepo.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            try {
                PaymentStatus status = PaymentStatus.valueOf(statusFilter);
                pageResult = txRepo.findByStatusOrderByCreatedAtDesc(status, pageable);
            } catch (IllegalArgumentException e) {
                pageResult = txRepo.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        return pageResult.map(this::toAdminDTO);
    }

    // ============================================================
    // DETAIL
    // ============================================================
    @Transactional(readOnly = true)
    public PaymentAdminDTO getDetail(Long id) {
        PaymentTransaction tx = txRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));
        return toAdminDTO(tx);
    }

    // ============================================================
    // APPROVE
    // ============================================================
    @Transactional
    public PaymentAdminDTO approve(Long txId, Long adminId) {
        PaymentTransaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));

        if (tx.getStatus() != PaymentStatus.WAITING_VERIFICATION) {
            throw new IllegalStateException(
                    "Hanya transaksi WAITING_VERIFICATION yang bisa di-approve. Status: "
                            + tx.getStatus()
            );
        }

        tx.setStatus(PaymentStatus.PAID);
        tx.setVerifiedBy(adminId);
        tx.setVerifiedAt(LocalDateTime.now());
        tx.setPaidAt(LocalDateTime.now());
        tx.setRejectionReason(null);
        PaymentTransaction saved = txRepo.save(tx);

        User user = userRepo.findById(tx.getUserId())
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));

        user.setPaymentStatus(UserPaymentStatus.PAID);
        user.setPaidAt(LocalDateTime.now());

        Boolean autoApprove = settingService.getSettings()
                .getAutoApproveAfterPayment();
        boolean auto = Boolean.TRUE.equals(autoApprove);

        if (auto) {
            user.setStatus(UserStatus.ACTIVE);
            user.setApprovedAt(LocalDateTime.now());
            user.setApprovedBy(adminId);
        } else {
            user.setStatus(UserStatus.PENDING);
        }

        userRepo.save(user);

        log.info("✅ Payment APPROVED: ref={}, user={}, nextStatus={}",
                tx.getReferenceId(), user.getUsername(), user.getStatus());

        // ⭐ Log activity
        User admin = userRepo.findById(adminId).orElse(null);
        activityLog.logPaymentApproved(admin, tx.getId(), tx.getReferenceId());

        // Publish event → email
        eventPublisher.publishEvent(new PaymentSuccessEvent(
                user.getId(),
                tx.getReferenceId(),
                user.getUsername(),
                user.getEmail(),
                user.getPortfolioSlug(),
                "REGISTRATION"
        ));

        return toAdminDTO(saved);
    }

    // ============================================================
    // REJECT
    // ============================================================
    @Transactional
    public PaymentAdminDTO reject(Long txId, Long adminId, String reason) {
        PaymentTransaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Transaksi tidak ditemukan"
                ));

        if (tx.getStatus() != PaymentStatus.WAITING_VERIFICATION) {
            throw new IllegalStateException(
                    "Hanya transaksi WAITING_VERIFICATION yang bisa di-reject. Status: "
                            + tx.getStatus()
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Alasan reject wajib diisi");
        }

        tx.setStatus(PaymentStatus.REJECTED);
        tx.setVerifiedBy(adminId);
        tx.setVerifiedAt(LocalDateTime.now());
        tx.setRejectionReason(reason);
        PaymentTransaction saved = txRepo.save(tx);

        User user = userRepo.findById(tx.getUserId())
                .orElseThrow(() -> new IllegalStateException("User tidak ditemukan"));

        user.setPaymentStatus(UserPaymentStatus.REJECTED);
        userRepo.save(user);

        log.info("❌ Payment REJECTED: ref={}, user={}, reason={}",
                tx.getReferenceId(), user.getUsername(), reason);

        // ⭐ Log activity
        User admin = userRepo.findById(adminId).orElse(null);
        activityLog.logPaymentRejected(
                admin, tx.getId(), tx.getReferenceId(), reason);

        // ⭐ Publish event → email
        eventPublisher.publishEvent(new PaymentRejectedEvent(
                user.getId(),
                tx.getReferenceId(),
                user.getUsername(),
                user.getEmail(),
                user.getPortfolioSlug(),
                reason
        ));

        return toAdminDTO(saved);
    }

    // ============================================================
    // STATS
    // ============================================================
    @Transactional(readOnly = true)
    public PaymentStatsDTO getStats() {
        long pending = txRepo.countByStatus(PaymentStatus.PENDING)
                + txRepo.countByStatus(PaymentStatus.WAITING_VERIFICATION);
        long paid = txRepo.countByStatus(PaymentStatus.PAID);
        long rejected = txRepo.countByStatus(PaymentStatus.REJECTED);
        long expired = txRepo.countByStatus(PaymentStatus.EXPIRED);

        LocalDateTime startOfMonth = YearMonth.now()
                .atDay(1)
                .atStartOfDay();

        BigDecimal revenueMonth = txRepo.sumPaidAmountSince(startOfMonth);
        BigDecimal revenueTotal = txRepo.sumPaidAmountSince(
                LocalDateTime.of(2000, 1, 1, 0, 0)
        );

        return PaymentStatsDTO.builder()
                .totalPending(pending)
                .totalPaid(paid)
                .totalRejected(rejected)
                .totalExpired(expired)
                .revenueThisMonth(revenueMonth != null ? revenueMonth : BigDecimal.ZERO)
                .revenueTotal(revenueTotal != null ? revenueTotal : BigDecimal.ZERO)
                .build();
    }

    // ============================================================
    // MAPPER
    // ============================================================
    private PaymentAdminDTO toAdminDTO(PaymentTransaction tx) {
        User user = userRepo.findById(tx.getUserId()).orElse(null);
        PaymentMethod method = tx.getPaymentMethodId() != null
                ? methodRepo.findById(tx.getPaymentMethodId()).orElse(null)
                : null;
        User verifier = tx.getVerifiedBy() != null
                ? userRepo.findById(tx.getVerifiedBy()).orElse(null)
                : null;

        PaymentAdminDTO.PaymentAdminDTOBuilder b = PaymentAdminDTO.builder()
                .id(tx.getId())
                .referenceId(tx.getReferenceId())
                .userId(tx.getUserId())
                .paymentMethodId(tx.getPaymentMethodId())
                .methodType(tx.getMethodType().name())
                .methodLabel(tx.getMethodLabel())
                .amountIdr(tx.getAmountIdr())
                .status(tx.getStatus().name())
                .proofImageUrl(tx.getProofImageUrl())
                .proofNote(tx.getProofNote())
                .proofUploadedAt(tx.getProofUploadedAt())
                .proofUploadCount(tx.getProofUploadCount())
                .verifiedBy(tx.getVerifiedBy())
                .verifiedAt(tx.getVerifiedAt())
                .rejectionReason(tx.getRejectionReason())
                .expiredAt(tx.getExpiredAt())
                .paidAt(tx.getPaidAt())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt());

        if (user != null) {
            b.username(user.getUsername())
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .portfolioSlug(user.getPortfolioSlug())
                    .userStatus(user.getStatus().name())
                    .userPaymentStatus(user.getPaymentStatus().name());
        }

        if (verifier != null) {
            b.verifiedByName(verifier.getDisplayName() != null
                    ? verifier.getDisplayName()
                    : verifier.getUsername());
        }

        if (method != null) {
            b.qrisImageUrl(method.getQrisImageUrl())
                    .qrisMerchantName(method.getQrisMerchantName())
                    .bankName(method.getBankName())
                    .bankAccountNumber(method.getBankAccountNumber())
                    .bankAccountHolder(method.getBankAccountHolder())
                    .ewalletProvider(method.getEwalletProvider())
                    .ewalletAccount(method.getEwalletAccount())
                    .ewalletAccountHolder(method.getEwalletAccountHolder())
                    .cryptoChain(method.getCryptoChain())
                    .cryptoToken(method.getCryptoToken())
                    .cryptoAddress(method.getCryptoAddress())
                    .cryptoNetworkNote(method.getCryptoNetworkNote());
        }

        return b.build();
    }
}