package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

public class Exist<T> implements Validation<T> {

    @Override
    public ValidationResult validity(T s) {
        if (s != null) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(ValidationError.builder().message("required value is null"));
    }

    @Override
    public boolean nullable() {
        return false;
    }

}
