package io.nubespark.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JSONUtils {
    public static JsonObject getMatchValueOrDefaultOne(JsonArray jsonArray, String _id) {
        for (Object object : jsonArray) {
            JsonObject jsonObject = (JsonObject) object;
            if (jsonObject.getString("_id").equals(_id)) {
                return jsonObject;
            }
        }
        return jsonArray.getJsonObject(0);
    }
}
