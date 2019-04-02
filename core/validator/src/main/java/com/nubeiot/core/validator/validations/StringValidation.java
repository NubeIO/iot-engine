package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.DataTypeValidation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringValidation<T> extends DataTypeValidation<T> {

    @Override
    protected Class classType() {
        return String.class;
    }

}
