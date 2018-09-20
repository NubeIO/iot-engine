package io.nubespark.utils;

import io.vertx.core.json.JsonObject;

import java.util.List;

public class JSONUtils {
    public static JsonObject getMatchValueOrDefaultOne(List<JsonObject> jsonObjectList, String _id) {
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            if (jsonObject.getString("_id").equals(_id)) {
                return jsonObject;
            }
        }
        return jsonObjectList.get(0);
    }
}
