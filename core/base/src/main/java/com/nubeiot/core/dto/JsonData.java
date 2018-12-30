package com.nubeiot.core.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JsonData {

    default JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz) {
        return from(object, clazz, "Invalid data format");
    }

    static <T extends JsonData> T from(Object object, Class<T> clazz, String errorMsg) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : JsonObject.mapFrom(Objects.requireNonNull(object));
            return entries.mapTo(clazz);
        } catch (IllegalArgumentException | NullPointerException | DecodeException ex) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, errorMsg, new HiddenException(ex));
        }
    }

}
