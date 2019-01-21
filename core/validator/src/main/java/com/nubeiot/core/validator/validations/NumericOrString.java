package com.nubeiot.core.validator.validations;

import java.util.Arrays;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class NumericOrString<T> extends Validation<T, Object> {

    @Override
    public Single<ValidationResult<Object>> validate(T s) {
        return new Alternative<>(Arrays.asList(new Int<>(), new Dbl<>(), new Str<>())).registerField(
            field).registerParentField(parentField).validate(s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
