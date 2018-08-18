package io.nubespark;

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
            if (fieldType.equals("int")) {
                jsonObject.put(fieldName, input.get("body").getInteger(fieldName, 0));
            } else if (fieldType.equals("String"))  {
                jsonObject.put(fieldName, input.get("body").getString(fieldName, ""));
            }
            System.out.println(fieldType + " ::: "+ fieldName);
        }
        System.out.println("Model is :::" + jsonObject);
        return jsonObject;
    }
}
