package com.nubeiot.core.validator.validations;

import java.util.Arrays;
import java.util.List;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Required<T> extends Validation<T, List<?>> {

    private final Validation<T, ?> validation;

    @Override
    public Single<ValidationResult<List<?>>> validity(T s) {
        return new Composition<>(Arrays.asList(validation, new Exist<>())).registerInput(this.input).validate(s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    protected boolean isNullable() {
        return false;
    }

}
