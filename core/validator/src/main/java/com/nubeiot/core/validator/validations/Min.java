package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;

public class Min<T> extends Max<T> {

    private final Double value;

    public Min(Double value) {
        super(value);
        this.value = value;
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" field value is not greater than {2}", errorType, getAbsoluteField(), value);
    }

    protected boolean condition(Double d) {
        return value <= d;
    }

}
