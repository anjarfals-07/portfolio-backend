package com.anjar.portfolio.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReservedUsernameValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotReservedUsername {
    String message() default "Username ini tidak tersedia, coba yang lain";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}