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
    public Single<ValidationResult<List<?>>> validate(T s) {
        return new Composition<>(Arrays.asList(validation, new Exist<>())).registerParentField(
            parentField).registerField(field).validate(s);
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

    protected boolean passNullCase() {
        return false;
    }

}
