package com.anjar.portfolio.util;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * Generator referenceId untuk PaymentTransaction.
 *
 * Format: PAY-{userId}-{timestamp}-{random6}
 * Contoh: PAY-9-1730000000000-A1B2C3
 *
 * Kenapa format ini?
 * - "PAY" → prefix jelas
 * - userId → gampang debug & trace
 * - timestamp → sorting natural
 * - random6 → cegah collision kalau user create 2 transaksi di detik sama
 */
public final class ReferenceIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_LENGTH = 6;

    private ReferenceIdGenerator() {}

    public static String generate(Long userId) {
        long ts = Instant.now().toEpochMilli();
        String random = randomString(RANDOM_LENGTH);
        return String.format("PAY-%d-%d-%s", userId, ts, random);
    }

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}