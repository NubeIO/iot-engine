package io.nubespark;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class Model {
    public Map<String, JsonObject> input = new HashMap<>();

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field: fields) {
            String fieldName  = field.getName();
            String fieldType  = field.getType().getSimpleName();
            System.out.println("Field type is this:::" + fieldType + " ::: " + fieldName);
            if (fieldType.equals("int")) {
                jsonObject.put(fieldName, input.get("body").getInteger(fieldName, 0));
            } else if (fieldType.equals("String"))  {
                jsonObject.put(fieldName, input.get("body").getString(fieldName, ""));
            } else if (fieldType.equals("String[]")) {
                jsonObject.put(fieldName, input.get("body").getJsonArray(fieldName, new JsonArray("[]")));
            }
        }
        return jsonObject;
    }
}
