package com.nubeiot.core.utils;

import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import io.vertx.core.json.JsonObject;

public class JsonUtils {

    public static Object getObject(JsonObject s, String field) {
        String[] fields = field.split("\\.");
        Object output = null;

        try {
            for (int i = 0; i < fields.length - 1; i++) {
                s = s.getJsonObject(fields[i]);
            }
            output = s.getValue(fields[fields.length - 1]);
        } catch (NullPointerException | ClassCastException ignored) {
        }
        return output;
    }

    public static boolean compareJsonObject(JsonObject expected, JsonObject actual) {
        if (expected == null && actual == null) {
            return true;
        } else if (expected == null || actual == null) {
            return false;
        }

        JSONCompareResult result = null;
        try {
            result = JSONCompare.compareJSON(expected.encode(), actual.encode(), JSONCompareMode.STRICT);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return result != null && result.passed();
    }

}
