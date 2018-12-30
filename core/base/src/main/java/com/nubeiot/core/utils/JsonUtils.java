package com.nubeiot.core.utils;

import java.util.ArrayList;
import java.util.Collection;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    @SuppressWarnings("unchecked")
    public static @NonNull JsonObject toJson(Object obj) {
        if (obj instanceof JsonObject) {
            return (JsonObject) obj;
        }
        if (obj instanceof Collection) {
            return new JsonObject().put("data", new JsonArray(new ArrayList((Collection) obj)));
        }
        try {
            return JsonObject.mapFrom(obj);
        } catch (IllegalArgumentException e) {
            return new JsonObject().put("data", obj);
        }
    }

}
