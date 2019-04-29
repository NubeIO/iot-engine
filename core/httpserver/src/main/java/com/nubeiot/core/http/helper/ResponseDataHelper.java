package com.nubeiot.core.http.helper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.handler.ResponseDataWriter;

@Deprecated
public class ResponseDataHelper {

    public static ResponseData unauthorized() {
        return ResponseDataWriter.serializeResponseData(new JsonObject().put("message", "Unauthorized").encode())
                                 .setStatus(HttpResponseStatus.UNAUTHORIZED);
    }

    public static ResponseData forbidden() {
        return ResponseDataWriter.serializeResponseData(new JsonObject().put("message", "Forbidden").encode())
                                 .setStatus(HttpResponseStatus.FORBIDDEN);
    }

    public static ResponseData badRequest(String message) {
        return ResponseDataWriter.serializeResponseData(new JsonObject().put("message", message).encode())
                                 .setStatus(HttpResponseStatus.BAD_REQUEST);
    }

    public static ResponseData internalServerError(String message) {
        return ResponseDataWriter.serializeResponseData(new JsonObject().put("message", message).encode())
                                 .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

}
