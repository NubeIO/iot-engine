package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.DataTypeValidation;

public class NumberValidation<T> extends DataTypeValidation<T> {

    @Override
    protected Class classType() {
        return Number.class;
    }

}
