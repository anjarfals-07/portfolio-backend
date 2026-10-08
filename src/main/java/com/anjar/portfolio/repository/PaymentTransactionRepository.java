package com.anjar.portfolio.repository;

import com.anjar.portfolio.entity.PaymentTransaction;
import com.anjar.portfolio.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByReferenceId(String referenceId);

    Optional<PaymentTransaction> findByUserIdAndStatus(Long userId, PaymentStatus status);

    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    Page<PaymentTransaction> findByStatusOrderByCreatedAtDesc(
            PaymentStatus status, Pageable pageable);

    Page<PaymentTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT t FROM PaymentTransaction t
        WHERE t.status = com.anjar.portfolio.enums.PaymentStatus.PENDING
          AND t.expiredAt IS NOT NULL
          AND t.expiredAt < :now
    """)
    List<PaymentTransaction> findExpiredPending(@Param("now") LocalDateTime now);

    long countByStatus(PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(t.amountIdr), 0)
        FROM PaymentTransaction t
        WHERE t.status = com.anjar.portfolio.enums.PaymentStatus.PAID
          AND t.paidAt >= :since
    """)
    BigDecimal sumPaidAmountSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT COUNT(t) > 0 FROM PaymentTransaction t
        WHERE t.userId = :userId
          AND t.status IN (
              com.anjar.portfolio.enums.PaymentStatus.PENDING,
              com.anjar.portfolio.enums.PaymentStatus.WAITING_VERIFICATION
          )
    """)
    boolean hasActiveTransaction(@Param("userId") Long userId);

    Optional<PaymentTransaction> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    // ⭐ Untuk generate reference ID unik
    boolean existsByReferenceId(String referenceId);
}