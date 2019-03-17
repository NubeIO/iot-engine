package com.nubeiot.core.dto;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventMessage;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ResponseData implements DataTransferObject {

    private JsonObject headers = new JsonObject();
    private JsonObject body = new JsonObject();

    //TODO convert EventMessage to ResponseData
    public static ResponseData from(EventMessage message) {
        return null;
    }

    @Override
    public JsonObject body() {
        return body;
    }

    @Override
    public JsonObject headers() {
        return headers;
    }

    public ResponseData setBody(JsonObject body) {
        this.body = body;
        return this;
    }

    public ResponseData setHeaders(JsonObject headers) {
        this.headers = headers;
        return this;
    }

}
