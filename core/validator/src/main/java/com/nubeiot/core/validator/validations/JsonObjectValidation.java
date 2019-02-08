package com.nubeiot.core.validator.validations;

import com.nubeiot.core.validator.DataTypeValidation;

import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonObjectValidation<T> extends DataTypeValidation<T> {

    @Override
    protected Class classType() {
        return JsonObject.class;
    }

}
