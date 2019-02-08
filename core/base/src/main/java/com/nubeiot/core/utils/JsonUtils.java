package com.nubeiot.core.utils;

import java.util.Map;
import java.util.function.BiFunction;

import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonUtils {

    /**
     * JsonObject @source gets merge with the JsonObject @defaultValue.
     */
    public static BiFunction<Object, Object, Object> mergeJsonObjectFunc = (source, defaultValue) -> {
        ((JsonObject) defaultValue).forEach(
            (defaultValueMap) -> iterateMergeFunction((JsonObject) source, defaultValueMap));
        return source;
    };

    public static Object getObject(JsonObject s, String field, Object defaultValue) {
        Object output = getObject(s, field);
        if (output == null) {
            output = defaultValue;
        }
        return output;
    }

    public static Object getObject(JsonObject s, String field) {
        String[] fields;
        if (Strings.isNotBlank(field)) {
            fields = field.split("\\.");
        } else {
            return s;
        }

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

    private static void iterateMergeFunction(JsonObject source, Map.Entry<String, Object> defaultValueMap) {
        Object defaultValue = defaultValueMap.getValue();
        String defaultKey = defaultValueMap.getKey();
        JsonObject sourceObject = source;
        Object sourceValue = sourceObject.getValue(defaultKey);
        if (defaultValue instanceof JsonObject && (sourceValue instanceof JsonObject || sourceValue == null)) {
            if (sourceValue == null) {
                sourceObject.put(defaultKey, new JsonObject());
            }
            sourceObject = sourceObject.getJsonObject(defaultKey);
            for (Map.Entry<String, Object> defaultValueMapChild : (JsonObject) defaultValue) {
                iterateMergeFunction(sourceObject, defaultValueMapChild);
            }
        } else if (defaultValue instanceof JsonArray && (sourceValue instanceof JsonArray || sourceValue == null)) {
            if (sourceValue == null) {
                sourceObject.put(defaultKey, new JsonArray());
            }
            JsonArray sourceArray = sourceObject.getJsonArray(defaultKey);
            iterateJsonArrayMergeFunction(sourceArray, (JsonArray) defaultValue);
        } else if (!(defaultValue instanceof Iterable) && !(sourceValue instanceof Iterable)) {
            if (sourceValue == null) {
                sourceObject.put(defaultKey, defaultValue);
            }
        }
    }

    private static void iterateJsonArrayMergeFunction(JsonArray sourceArray, JsonArray defaultValueArray) {
        Object defaultValue = defaultValueArray.getValue(0);
        if (defaultValue instanceof JsonObject) {
            if (sourceArray.size() == 0) {
                sourceArray.add(new JsonObject());
            }
            for (Object o : sourceArray) {
                ((JsonObject) defaultValue).forEach(
                    defaultValueMapChild -> iterateMergeFunction((JsonObject) o, defaultValueMapChild));
            }
        } else if (defaultValue instanceof JsonArray) {
            if (sourceArray.size() == 0) {
                sourceArray.add(new JsonArray());
            }
            for (Object o : sourceArray) {
                if (o instanceof JsonArray) {
                    iterateJsonArrayMergeFunction((JsonArray) o, (JsonArray) defaultValue);
                }
            }
        } else {
            if (sourceArray.size() == 0) {
                sourceArray.add(defaultValue);
            }
        }
    }

}

