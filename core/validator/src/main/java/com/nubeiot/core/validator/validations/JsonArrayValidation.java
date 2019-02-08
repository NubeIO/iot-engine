package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.DataTypeValidation;

import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonArrayValidation<T> extends DataTypeValidation<T> {

    @Override
    protected Class classType() {
        return JsonArray.class;
    }

}
