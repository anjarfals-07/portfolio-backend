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
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final boolean active;

    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole().name();
        this.active = Boolean.TRUE.equals(user.getActive());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security: authority harus prefix "ROLE_"
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