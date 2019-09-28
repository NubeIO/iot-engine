package com.nubeiot.edge.connector.bacnet.objectModels;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.iotdata.dto.PointPriorityValue;

import lombok.Getter;
import lombok.Setter;

/**
 * @deprecated Use {@link PointPriorityValue}
 */
@Getter
@Setter
@Deprecated
public class EdgePoint {

    private String id;
    private String name;
    private Object value;
    private Integer priority = null;
    private Object[] priorityArray = null;
    private Float covTolerance = 0f;
    private Kind kind = Kind.OTHER;


    public enum Kind {
        NUMBER, BOOL, OTHER
    }

    public EdgePoint(String id, Object value) {
        this.id = id;
        this.name = id;
        this.value = value;
    }

    public EdgePoint(String id, String name, Object value) {
        this(id, value);
        if (name != null) {
            this.name = name;
        }
    }

    public EdgePoint(String id, String name, Object value, Integer priority) {
        this(id, name, value);
        this.priority = priority;
    }

    public EdgePoint(String id, String name, Object value, Integer priority, String kind) {
        this(id, name, value, priority);
        try {
            this.kind = Kind.valueOf(kind.toUpperCase());
        } catch (Exception e) {
            this.kind = Kind.OTHER;
        }
    }

    public EdgePoint(String id, String name, Object value, Integer priority, String kind, Float covTolerance) {
        this(id, name, value, priority, kind);
        this.covTolerance = covTolerance;
    }

    public static EdgePoint fromJson(String id, JsonObject json) {
        String name = json.getString("name");
        Object value = json.getValue("value");
        Integer priority;
        try {
            priority = json.getInteger("priority");
        } catch (ClassCastException ex) {
            priority = new Integer(16);
        }

        String kind = json.getString("kind");

        Float covTolerance = 0f;
        if (json.containsKey("historySettings") && json.getJsonObject("historySettings").containsKey("tolerance")) {
            covTolerance = json.getJsonObject("historySettings").getFloat("tolerance");
        }

        EdgePoint point = new EdgePoint(id, name, value, priority, kind, covTolerance);
        if (json.containsKey("priorityArray")) {
            writePriorityArray(point, json.getJsonObject("priorityArray"));
        }

        return point;
    }

    private static void writePriorityArray(EdgePoint point, JsonObject priorityArray) {
        if (priorityArray == null) {
            return;
        }

        Object[] arr = new Object[16];
        for (int i = 0; i < 16; i++) {
            arr[i] = priorityArray.getValue(Integer.toString(i + 1));
        }
        point.priorityArray = arr;
    }

    public String toString() {
        JsonObject json = new JsonObject().put("id", this.id).put("name", this.name).put("value", this.value);
        if (priority != null) {
            json.put("priority", this.priority);
            JsonArray arr = new JsonArray();
            for (Object o : priorityArray) {
                if (o == null) {
                    o = "null";
                }
                arr.add(o);
            }
            json.put("priorityArray", arr);
        }
        return json.encodePrettily();
    }

}
