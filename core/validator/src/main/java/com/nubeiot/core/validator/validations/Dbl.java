package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Dbl<T> extends Validation<T, Double> {

    @Override
    public Single<ValidationResult<Double>> validate(T s) {
        if (s instanceof Double) {
            return new ValidationResult<Double>().asyncSuccess((Double) s);
        }
        return new ValidationResult<Double>().asyncInvalid(getErrorMessage());
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not the type of Double", errorType, getAbsoluteField());
    }

}
