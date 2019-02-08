package com.nubeiot.core.validator.validations;

import java.util.Arrays;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Required<T> implements Validation<T> {

    private final Validation<T> validation;

    @Override
    public ValidationResult validity(T s) {
        return new Composition<>(Arrays.asList(validation, new Exist<>())).validate(s);
    }

    public boolean nullable() {
        return false;
    }

}
