package com.nubeiot.core.common.utils;

import java.util.List;

import io.vertx.core.json.JsonObject;

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
