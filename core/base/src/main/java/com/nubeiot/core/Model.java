package com.nubeiot.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public abstract class Model {
    public Map<String, JsonObject> input = new HashMap<>();

    public Model(JsonObject body) {
        input.put("body", body);
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        Field[] fields = this.getClass().getDeclaredFields();

        // For setting JSON value in toJsonObject
        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldType = field.getType().getSimpleName();
            switch (fieldType) {
                case "int":
                    jsonObject.put(fieldName, input.get("body").getInteger(fieldName, 0));
                    break;
                case "String":
                    jsonObject.put(fieldName, input.get("body").getString(fieldName, ""));
                    break;
                case "Boolean":
                    jsonObject.put(fieldName, input.get("body").getBoolean(fieldName, false));
                    break;
                case "Double":
                    jsonObject.put(fieldName, input.get("body").getDouble(fieldName, 0d));
                    break;
                case "String[]":
                    jsonObject.put(fieldName, input.get("body").getJsonArray(fieldName, new JsonArray()));
                    break;
                case "GeoPoint":
                    JsonObject geoPointJson = input.get("body").getJsonObject(fieldName, new JsonObject());
                    GeoPoint geoPoint = new GeoPoint(geoPointJson.getDouble("lng", 151.209900d), geoPointJson.getDouble("lat", -33.865143d));
                    jsonObject.put(fieldName, geoPoint.toJsonObject());
                    break;
            }
        }
        return jsonObject;
    }
}
