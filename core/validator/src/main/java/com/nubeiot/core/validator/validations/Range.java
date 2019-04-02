package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.ValidationError;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.core.validator.Validation;
import com.nubeiot.core.validator.ValidationResult;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Range<T> implements Validation<T> {

    private final Double minValue;
    private final Double maxValue;

    @Override
    public ValidationResult validity(T s) {
        if (s instanceof Number) {
            Double d = Double.parseDouble(s.toString());
            if (minValue <= d && d < maxValue) {
                return ValidationResult.valid();
            } else {
                return ValidationResult.invalid(ValidationError.builder()
                                                               .value(s.toString())
                                                               .message(
                                                                   Strings.format("should be on range [{0}<= x < {1}]",
                                                                                  minValue, maxValue)));
            }
        } else {
            return ValidationResult.invalid(ValidationError.builder()
                                                           .errorType("ClassCastException")
                                                           .value(s.toString())
                                                           .message("is not parsable to Number"));
        }
    }

}
