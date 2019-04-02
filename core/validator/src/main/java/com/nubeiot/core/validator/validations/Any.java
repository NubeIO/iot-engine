package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

public class Any<T> implements Validation<T> {

    public ValidationResult validate() {
        return this.validate(null);
    }

    @Override
    public ValidationResult validity(T s) {
        return ValidationResult.valid();
    }

}
