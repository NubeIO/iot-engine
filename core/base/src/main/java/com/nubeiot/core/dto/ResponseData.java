package com.nubeiot.core.dto;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventMessage;

import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
public class ResponseData implements DataTransferObject {

    private JsonObject headers = new JsonObject();
    private JsonObject body = new JsonObject();

    public ResponseData(JsonObject headers, JsonObject body) {
        this.headers = Objects.nonNull(headers) ? headers : new JsonObject();
        this.body = body;
    }

    //TODO convert EventMessage to ResponseData
    public static ResponseData from(@NonNull EventMessage message) {
        ResponseData responseData = new ResponseData();
        responseData.setHeaders(new JsonObject().put("status", message.getStatus())
                                                .put("action", message.getAction())
                                                .put("prevAction", message.getPrevAction()));
        if (message.isError()) {
            return responseData.setBody(message.getError().toJson());
        }
        return responseData.setBody(message.getData());
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
