package com.nubeiot.core.validator.validations;

import java.util.Arrays;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

public class NumberOrStringValidation<T> implements Validation<T> {

    @Override
    public ValidationResult validity(T s) {
        return new Alternative<>(Arrays.asList(new NumberValidation<>(), new StringValidation<>())).validate(s);
    }

}
