package com.anjar.portfolio.service;

import com.anjar.portfolio.dto.AddDomainRequest;
import com.anjar.portfolio.dto.TenantDomainDTO;
import com.anjar.portfolio.entity.TenantDomain;
import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.enums.SSLStatus;
import com.anjar.portfolio.exception.BadRequestException;
import com.anjar.portfolio.exception.ResourceNotFoundException;
import com.anjar.portfolio.repository.TenantDomainRepository;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantDomainService {

    private static final int MAX_DOMAINS_PER_USER = 5;

    private final TenantDomainRepository domainRepository;
    private final UserRepository userRepository;
    private final DomainVerificationService verificationService;

    // ============================================================
    // LIST DOMAINS (owner)
    // ============================================================

    @Transactional(readOnly = true)
    public List<TenantDomainDTO> listDomains(Long userId) {
        return domainRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ADD DOMAIN (owner)
    // ============================================================

    @Transactional
    public TenantDomainDTO addDomain(Long userId, AddDomainRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String domain = normalizeDomain(request.getDomain());

        // Validasi format
        if (!isValidDomain(domain)) {
            throw new BadRequestException("Format domain tidak valid: " + domain);
        }

        // Cek duplikat
        if (domainRepository.existsByDomainIgnoreCase(domain)) {
            throw new BadRequestException("Domain sudah dipakai: " + domain);
        }

        // Cek limit
        long count = domainRepository.countByUserId(userId);
        if (count >= MAX_DOMAINS_PER_USER) {
            throw new BadRequestException(
                    "Maksimal " + MAX_DOMAINS_PER_USER + " domain per akun"
            );
        }

        // Domain pertama jadi primary
        List<TenantDomain> existing = domainRepository.findByUserIdOrderByCreatedAtAsc(userId);
        boolean isFirst = existing.isEmpty();

        // Generate token
        String token = verificationService.generateVerificationToken();

        TenantDomain entity = TenantDomain.builder()
                .user(user)
                .domain(domain)
                .isPrimary(isFirst)
                .isVerified(false)
                .verificationToken(token)
                .sslStatus(SSLStatus.PENDING)
                .build();

        TenantDomain saved = domainRepository.save(entity);
        log.info("✅ Domain added: {} for user {}", domain, user.getUsername());

        return toDTO(saved);
    }

    // ============================================================
    // VERIFY DOMAIN (owner)
    // ============================================================

    @Transactional
    public TenantDomainDTO verifyDomain(Long userId, Long domainId) {
        TenantDomain entity = getDomainOwnedBy(domainId, userId);

        entity.setLastCheckedAt(LocalDateTime.now());

        // Cek DNS — CNAME/A record atau TXT record
        boolean cnameOk = verificationService.verifyCname(entity.getDomain());
        boolean txtOk = verificationService.verifyTxtRecord(
                entity.getDomain(),
                entity.getVerificationToken()
        );

        boolean verified = cnameOk || txtOk;

        if (verified) {
            entity.setIsVerified(true);
            entity.setVerifiedAt(LocalDateTime.now());
            entity.setSslStatus(SSLStatus.ACTIVE);
            log.info("✅ Domain verified: {}", entity.getDomain());
        } else {
            log.warn("⚠️ Domain verification failed: {} (cname={}, txt={})",
                    entity.getDomain(), cnameOk, txtOk);
        }

        TenantDomain saved = domainRepository.save(entity);
        return toDTO(saved);
    }

    // ============================================================
    // DELETE DOMAIN (owner)
    // ============================================================

    @Transactional
    public void deleteDomain(Long userId, Long domainId) {
        TenantDomain entity = getDomainOwnedBy(domainId, userId);

        boolean wasPrimary = Boolean.TRUE.equals(entity.getIsPrimary());

        domainRepository.delete(entity);
        log.info("🗑️ Domain deleted: {} (user {})", entity.getDomain(), userId);

        // Kalau primary dihapus, promosikan domain lain jadi primary
        if (wasPrimary) {
            List<TenantDomain> remaining =
                    domainRepository.findByUserIdOrderByCreatedAtAsc(userId);
            if (!remaining.isEmpty()) {
                TenantDomain newPrimary = remaining.get(0);
                newPrimary.setIsPrimary(true);
                domainRepository.save(newPrimary);
                log.info("⭐ New primary domain: {}", newPrimary.getDomain());
            }
        }
    }

    // ============================================================
    // SET PRIMARY (owner)
    // ============================================================

    @Transactional
    public TenantDomainDTO setPrimary(Long userId, Long domainId) {
        TenantDomain entity = getDomainOwnedBy(domainId, userId);

        if (!Boolean.TRUE.equals(entity.getIsVerified())) {
            throw new BadRequestException("Domain belum terverifikasi");
        }

        // Unset primary untuk semua domain user
        List<TenantDomain> all = domainRepository.findByUserIdOrderByCreatedAtAsc(userId);
        for (TenantDomain td : all) {
            td.setIsPrimary(td.getId().equals(domainId));
        }
        domainRepository.saveAll(all);

        log.info("⭐ Primary domain set: {} (user {})", entity.getDomain(), userId);

        return toDTO(entity);
    }

    // ============================================================
    // PUBLIC LOOKUP (by domain)
    // ============================================================

    /**
     * Lookup tenant by domain (public).
     * Return Optional.empty() kalau tidak ditemukan / belum verified.
     */
    @Transactional(readOnly = true)
    public Optional<User> lookupByDomain(String domain) {
        String clean = normalizeDomain(domain);

        return domainRepository.findVerifiedByDomain(clean)
                .map(TenantDomain::getUser);
    }

    // ============================================================
    // HELPER
    // ============================================================

    private TenantDomain getDomainOwnedBy(Long domainId, Long userId) {
        TenantDomain entity = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Domain", domainId
                ));

        if (!entity.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Domain", domainId);
        }

        return entity;
    }

    private String normalizeDomain(String input) {
        if (input == null) return "";
        return input
                .toLowerCase()
                .trim()
                .replaceAll("^https?://", "")
                .replaceAll("^www\\.", "")
                .replaceAll("/.*$", "")
                .split(":")[0];
    }

    private boolean isValidDomain(String domain) {
        return domain.matches(
                "^([a-z0-9]([a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}$"
        );
    }

    private TenantDomainDTO toDTO(TenantDomain td) {
        return TenantDomainDTO.builder()
                .id(td.getId())
                .domain(td.getDomain())
                .isPrimary(td.getIsPrimary())
                .isVerified(td.getIsVerified())
                .sslStatus(td.getSslStatus())
                .verificationToken(td.getVerificationToken())
                .lastCheckedAt(td.getLastCheckedAt())
                .verifiedAt(td.getVerifiedAt())
                .createdAt(td.getCreatedAt())
                .cnameTarget(verificationService.getCnameTarget())
                .txtRecordName(verificationService.getTxtRecordName(td.getDomain()))
                .txtRecordValue(verificationService.getTxtRecordValue(td.getVerificationToken()))
                .build();
    }
}