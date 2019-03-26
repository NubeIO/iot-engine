package com.nubeiot.core.mongo;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class MongoUtils {

    public static String[] getIds(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids.toArray(new String[0]);
    }

    public static List<String> getIdsOnList(List<JsonObject> jsonObjectList) {
        List<String> _ids = new ArrayList<>();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }

    public static JsonArray getIdsOnJsonArray(List<JsonObject> jsonObjectList) {
        JsonArray _ids = new JsonArray();
        for (JsonObject jsonObject : jsonObjectList) {
            _ids.add(jsonObject.getString("_id"));
        }
        return _ids;
    }

    public static JsonObject getMatchValueOrFirstOne(List<JsonObject> jsonObjectList, String _id) {
        for (Object object : jsonObjectList) {
            JsonObject jsonObject = (JsonObject) object;
            if (jsonObject.getString("_id").equals(_id)) {
                return jsonObject;
            }
        }
        return jsonObjectList.size() > 0 ? jsonObjectList.get(0) : null;
    }

}
