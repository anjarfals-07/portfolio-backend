package com.anjar.portfolio.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validator KHUSUS untuk field `defaultPortfolioUsername` di settings.
 *
 * Bedanya dengan @NotReservedUsername:
 * - TIDAK cek reserved words (karena admin boleh set default ke user existing)
 * - HANYA cek format slug valid
 */
@Documented
@Constraint(validatedBy = PortfolioSlugValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPortfolioSlug {
    String message() default "Format slug tidak valid (cuma lowercase, angka, dan dash)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}