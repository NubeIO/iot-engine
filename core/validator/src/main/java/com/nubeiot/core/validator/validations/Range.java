package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import io.reactivex.Single;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Range<T> extends Validation<T, Double> {

    private final Double minValue;
    private final Double maxValue;

    @Override
    public Single<ValidationResult<Double>> validity(T s) {
        if (s instanceof Number) {
            Double d = Double.parseDouble(s.toString());
            if (minValue <= d && d <= maxValue) {
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
        return Strings.format("{0}: \"{1}\" should be on range {2}-{3}", getErrorType(), getInput(), minValue,
                              maxValue);
    }

}
