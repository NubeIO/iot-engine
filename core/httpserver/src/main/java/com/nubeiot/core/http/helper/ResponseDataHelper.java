package com.nubeiot.core.http.helper;

import static com.nubeiot.core.http.handler.ResponseDataWriter.responseData;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;

@Deprecated
public class ResponseDataHelper {

    public static ResponseData unauthorized() {
        return responseData(new JsonObject().put("message", "Unauthorized").encode()).setStatus(HttpResponseStatus.UNAUTHORIZED);
    }

    public static ResponseData forbidden() {
        return responseData(new JsonObject().put("message", "Forbidden").encode()).setStatus(HttpResponseStatus.FORBIDDEN);
    }

    public static ResponseData badRequest(String message) {
        return responseData(new JsonObject().put("message", message).encode()).setStatus(HttpResponseStatus.BAD_REQUEST);
    }

    public static ResponseData internalServerError(String message) {
        return responseData(new JsonObject().put("message", message).encode()).setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

}
