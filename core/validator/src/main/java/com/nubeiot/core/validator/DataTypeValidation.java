package com.nubeiot.core.validator;

import com.nubeiot.core.utils.Strings;

import io.reactivex.Single;

public abstract class DataTypeValidation<T> extends Validation<T, T> {

    public abstract Class classType();

    @Override
    public Single<ValidationResult<T>> validity(T s) {
        if (classType().isInstance(s)) {
            return ValidationResult.valid(s);
        }
        return ValidationResult.invalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" is not the type of {2}", getErrorType(), getInput(), classType().getName());
    }

}
