package io.nubespark;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Model {
    public Map<String, JsonObject> input = new HashMap<>();

    public Model(JsonObject body) {
        input.put("body", body);
    }

    protected  boolean uuidAsId() {
        return true;
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
                case "String[]":
                    jsonObject.put(fieldName, input.get("body").getJsonArray(fieldName, new JsonArray("[]")));
                    break;
            }
        }
        if (uuidAsId()) {
            jsonObject.put("_id", UUID.randomUUID().toString());
        }
        return jsonObject;
    }
}
