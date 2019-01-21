package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Str<T> extends Validation<T, String> {

    @Override
    public Single<ValidationResult<String>> validate(T s) {
        if (s instanceof String) {
            return new ValidationResult<String>().asyncSuccess((String) s);
        }
        return new ValidationResult<String>().asyncInvalid(getErrorMessage());
    }

    @Override
    public String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not the type of String", errorType, getAbsoluteField());
    }

}
