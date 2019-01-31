package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Max<T> extends Validation<T, Double> {

    protected final Double value;

    @Override
    public Single<ValidationResult<Double>> validity(T s) {
        if (s instanceof Number) {
            Double d = Double.parseDouble(s.toString());
            if (condition(d)) {
                return ValidationResult.valid(d);
            } else {
                return ValidationResult.invalid(getErrorMessage());
            }
        } else {
            return ValidationResult.invalid(
                Strings.format("ClassCastException: {0} is not parsable to Number", getInput()));
        }
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: {1} is not less than or equal to {2}", getErrorType(), getInput(), value);
    }

    protected boolean condition(Double d) {
        return value >= d;
    }

}
