package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Max<T> implements Validation<T> {

    protected final Double value;

    @Override
    public ValidationResult validity(T s) {
        if (s instanceof Number) {
            Double d = Double.parseDouble(s.toString());
            if (condition(d)) {
                return ValidationResult.valid();
            } else {
                return ValidationResult.invalid(getErrorMessage(s));
            }
        } else {
            return ValidationResult.invalid(ValidationError.builder()
                                                           .errorType("ClassCastException")
                                                           .value(s.toString())
                                                           .message("is not parsable to Number"));
        }
    }

    protected ValidationError.Builder getErrorMessage(T s) {
        return ValidationError.builder()
                              .value(s.toString())
                              .message("is not less than or equal to " + value.toString());
    }

    protected boolean condition(Double d) {
        return value >= d;
    }

}
