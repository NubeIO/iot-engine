package com.nubeiot.core.validator.validations;

import java.util.Arrays;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class NumberOrStringValidation<T> extends Validation<T, Object> {

    @Override
    public Single<ValidationResult<Object>> validity(T s) {
        return new Alternative<>(Arrays.asList(new NumberValidation<>(), new StringValidation<>())).registerInput(
            this.input).validate(s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
