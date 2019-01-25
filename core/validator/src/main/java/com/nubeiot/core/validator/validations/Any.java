package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Any<T> extends Validation<T, T> {

    public Single<ValidationResult<T>> validate() {
        return this.validate(null);
    }

    @Override
    public Single<ValidationResult<T>> validity(T s) {
        return ValidationResult.valid(s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
