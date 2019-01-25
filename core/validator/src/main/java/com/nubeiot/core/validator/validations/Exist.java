package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Exist<T> extends Validation<T, T> {

    @Override
    public Single<ValidationResult<T>> validity(T s) {
        if (s != null) {
            return ValidationResult.valid(s);
        }
        return ValidationResult.invalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: required value is null", getErrorType(), getInput());
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

}
