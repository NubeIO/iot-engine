package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Int<T> extends Validation<T, Integer> {

    @Override
    public Single<ValidationResult<Integer>> validate(T s) {
        if (s instanceof Integer) {
            return new ValidationResult<Integer>().asyncSuccess((Integer) s);
        }
        return new ValidationResult<Integer>().asyncInvalid(getErrorMessage());
    }

    @Override
    public String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not the type of Integer", errorType, getAbsoluteField());
    }

}
