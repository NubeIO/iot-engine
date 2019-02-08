package com.nubeiot.core.validator;

import com.nubeiot.core.exceptions.ValidationError;

public abstract class DataTypeValidation<T> implements Validation<T> {

    protected abstract Class classType();

    @Override
    public ValidationResult validity(T s) {
        if (classType().isInstance(s)) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(ValidationError.builder()
                                                       .value(s == null ? null : s.toString())
                                                       .message("is not the type of " + classType().getName()));
    }

}
