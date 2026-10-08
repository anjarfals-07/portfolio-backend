package com.anjar.portfolio.security;

import com.anjar.portfolio.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails untuk Spring Security.
 * Wrap User entity + include userId + role.
 *
 * Dipakai di:
 * 1. Form login — AuthenticationManager → DaoAuthenticationProvider
 * 2. JWT filter — set principal sebagai UserDetailsImpl
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean active;

    // ===== Constructor dari User entity (untuk login) =====
    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole().name();
        this.active = Boolean.TRUE.equals(user.getActive());
    }

    // ===== Constructor minimal (untuk JWT) =====
    public UserDetailsImpl(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.role = role;
        this.active = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}