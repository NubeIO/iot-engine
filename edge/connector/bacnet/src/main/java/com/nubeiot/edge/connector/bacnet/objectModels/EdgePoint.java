package com.nubeiot.edge.connector.bacnet.objectModels;

import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.utils.BACnetDataConversions;
import com.nubeiot.edge.connector.bacnet.utils.LocalPointObjectUtils;
import com.serotonin.bacnet4j.obj.BACnetObject;
import com.serotonin.bacnet4j.type.Encodable;
import com.serotonin.bacnet4j.type.constructed.PropertyValue;
import com.serotonin.bacnet4j.type.constructed.ValueSource;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;
import com.serotonin.bacnet4j.type.primitive.CharacterString;
import com.serotonin.bacnet4j.type.primitive.Real;
import com.serotonin.bacnet4j.type.primitive.UnsignedInteger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EdgePoint {

    private String id;
    private String name;
    private Object value;
    private Integer priority = null;
    private Object[] priorityArray = null;
    private Float covTolerance = 0f;

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

    public EdgePoint(String id, String name, Object value, Integer priority, Float covTolerance) {
        this(id, name, value, priority);
        this.covTolerance = covTolerance;
    }

    public static EdgePoint fromJson(String id, JsonObject json) {
        String name = json.getString("name");
        Object value = json.getValue("value");
        Integer priority = json.getInteger("priority");
        Float covTolerance = 0f;
        if (json.containsKey("historySettings") && json.getJsonObject("historySettings").containsKey("tolerance")) {
            covTolerance = json.getJsonObject("historySettings").getFloat("tolerance");
        }

        EdgePoint point = new EdgePoint(id, name, value, priority, covTolerance);
        if (priority != null && json.containsKey("priorityArray")) {
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
            arr[i] = priorityArray.getValue(Integer.toString(i+1));
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
