package com.anjar.portfolio.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {

    private final String username;
    private final String email;
    private final String slug;

    /**
     * ⭐ Kalau true, listener TIDAK kirim welcome email.
     * Email baru dikirim setelah payment sukses.
     */
    private final boolean requiresPayment;
}