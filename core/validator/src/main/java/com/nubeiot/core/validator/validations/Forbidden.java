package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Forbidden<T> extends Validation<T, Object> {

    public static String FORBIDDEN_MESSAGE = "Forbidden: you are not authorized to post this value";

    public Single<ValidationResult<Object>> validate() {
        return this.validate(null);
    }

    @Override
    public Single<ValidationResult<Object>> validate(T s) {
        return new ValidationResult<>().asyncInvalid(NubeException.ErrorCode.INSUFFICIENT_PERMISSION_ERROR,
                                                     getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return FORBIDDEN_MESSAGE;
    }

}
