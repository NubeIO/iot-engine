package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Forbidden<T> extends Validation<T, Object> {

    public Single<ValidationResult<Object>> validate() {
        return this.validate(null);
    }

    @Override
    public Single<ValidationResult<Object>> validity(T s) {
        this.setErrorType("Forbidden");
        this.setErrorCode(NubeException.ErrorCode.INSUFFICIENT_PERMISSION_ERROR);
        return ValidationResult.invalid(this.getErrorCode(), this.getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: you are not authorized to post this value", getErrorType());
    }

    @Override
    protected boolean isNullable() {
        return false;
    }

}
