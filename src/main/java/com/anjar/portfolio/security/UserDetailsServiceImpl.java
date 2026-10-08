package com.anjar.portfolio.security;

import com.anjar.portfolio.entity.User;
import com.anjar.portfolio.exception.AccountPendingException;
import com.anjar.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom UserDetailsService — load user by username untuk Spring Security.
 *
 * Cek:
 * - User exist
 * - Status user (PENDING / ACTIVE / REJECTED / SUSPENDED)
 * - Flag active
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Cari user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username
                ));

        // 2. Cek status user SEBELUM return UserDetails
        switch (user.getStatus()) {
            case PENDING -> {
                log.warn("⚠️ Login attempt by PENDING user: {}", username);
                throw new AccountPendingException(
                        "Akun masih menunggu approval admin"
                );
            }

            case REJECTED -> {
                log.warn("⚠️ Login attempt by REJECTED user: {}", username);
                String reason = user.getRejectionReason();
                String msg = "Akun ditolak oleh admin.";
                if (reason != null && !reason.isBlank()) {
                    msg += " Alasan: " + reason;
                }
                throw new BadCredentialsException(msg);
            }

            case SUSPENDED -> {
                log.warn("⚠️ Login attempt by SUSPENDED user: {}", username);
                throw new BadCredentialsException(
                        "Akun di-suspend. Hubungi admin."
                );
            }

            case ACTIVE -> {
                // Lanjut cek flag active
            }
        }

        // 3. Cek flag `active` (flag lama)
        if (Boolean.FALSE.equals(user.getActive())) {
            log.warn("⚠️ Login attempt by disabled user: {}", username);
            throw new BadCredentialsException("Akun di-disable. Hubungi admin.");
        }

        // 4. Return UserDetails
        return new UserDetailsImpl(user);
    }
}