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
    public Single<ValidationResult<Double>> validate(T s) {
        if (s instanceof Double || s instanceof Integer || s instanceof Long || s instanceof Float) {
            Double d = Double.parseDouble(s.toString());
            if (minValue <= d && d <= maxValue) {
                return new ValidationResult<Double>().asyncSuccess(d);
            } else {
                return new ValidationResult<Double>().asyncInvalid(getErrorMessage());
            }
        } else {
            return new ValidationResult<Double>().asyncInvalid(
                Strings.format("ClassCastException: \"{0}\" field value is not parsable to Number",
                               getAbsoluteField()));
        }
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value should be on range {2}-{3}", errorType, getAbsoluteField(),
                              minValue, maxValue);
    }

}
