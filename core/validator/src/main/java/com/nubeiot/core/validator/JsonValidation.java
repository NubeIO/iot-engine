package com.nubeiot.core.validator;

import com.nubeiot.core.utils.JsonUtils;
import com.nubeiot.core.utils.Strings;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface JsonValidation {

    default Object get(Object data, String key) {
        if (Strings.isBlank(key) || data instanceof JsonArray) {
            return data;
        }
        if (!(data instanceof JsonObject)) {
            return null;
        }
        return JsonUtils.getObject((JsonObject) data, key);
    }

}
