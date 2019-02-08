package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.DataTypeValidation;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IntegerValidation<T> extends DataTypeValidation<T> {

    @Override
    protected Class classType() {
        return Integer.class;
    }

}
