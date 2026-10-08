package com.anjar.portfolio.validator;

import com.anjar.portfolio.util.ReservedUsernames;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReservedUsernameValidator
        implements ConstraintValidator<NotReservedUsername, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        return !ReservedUsernames.isReserved(value);
    }
}