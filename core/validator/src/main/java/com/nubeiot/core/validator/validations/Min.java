package com.nubeiot.core.validator.validations;

import com.nubeiot.core.utils.Strings;

public class Min<T> extends Max<T> {

    public Min(Double value) {
        super(value);
    }

    @Override
    protected String getErrorMessage() {
        return Strings.format("{0}: \"{1}\" is not greater than or equal to {2}", getErrorType(), getInput(), value);
    }

    protected boolean condition(Double d) {
        return value <= d;
    }

}
