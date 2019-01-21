package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Any extends Validation {

    public Single<ValidationResult<Object>> validate() {
        return this.validate(null);
    }

    @Override
    public Single<ValidationResult<Object>> validate(Object s) {
        return new ValidationResult<>().asyncSuccess();
    }

    @Override
    protected String getErrorMessage() {
        return null;
    }

}
