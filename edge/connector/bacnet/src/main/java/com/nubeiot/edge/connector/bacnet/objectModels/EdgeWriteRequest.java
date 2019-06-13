package com.nubeiot.edge.connector.bacnet.objectModels;

import io.vertx.core.json.JsonObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EdgeWriteRequest {

    private String id;
    private Object value;
    private int priority = 16;

    public EdgeWriteRequest(String id, Object value) {
        this.id = id;
        this.value = value;
    }

    public EdgeWriteRequest(String id, Object value, int priority) {
        this(id, value);
        this.priority = priority;
    }

    public JsonObject toJson() {
        return new JsonObject().put("value", value).put("priority", priority);
    }

    public static EdgeWriteRequest fromJson(String id, JsonObject json) {
        if (json.containsKey("priority")) {
            return new EdgeWriteRequest(id, json.getValue("value"), json.getInteger("priority"));
        } else {
            return new EdgeWriteRequest(id, json.getValue("value"));
        }
    }

}
