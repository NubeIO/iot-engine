package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;

public class Exist<T> extends Validation<T, Object> {

    @Override
    public Single<ValidationResult<Object>> validate(T s) {
        if (s != null) {
            return new ValidationResult<>().asyncSuccess();
        }
        return new ValidationResult<>().asyncInvalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is required", errorType, getAbsoluteField());
    }

    @Override
    protected boolean passNullCase() {
        return false;
    }

}
