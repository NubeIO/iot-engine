package com.nubeiot.core.validator.validations;

import com.nubeiot.core.exceptions.ValidationError;

public class Min<T> extends Max<T> {

    public Min(Double value) {
        super(value);
    }

    protected ValidationError.Builder getErrorMessage(T s) {
        return ValidationError.builder()
                              .value(s.toString())
                              .message("is not greater than or equal to " + value.toString());
    }

    protected boolean condition(Double d) {
        return value <= d;
    }

}
