package com.nubeiot.dashboard.helpers;

import com.nubeiot.core.dto.ResponseData;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

public class ResponseDataHelper {

    public static ResponseData unauthorized() {
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code())
                    .setBodyMessage(new JsonObject().put("message", "Unauthorized").encode());
        return responseData;
    }

    public static ResponseData forbidden() {
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(HttpResponseStatus.FORBIDDEN.code())
                    .setBodyMessage(new JsonObject().put("message", "Forbidden").encode());
        return responseData;
    }

    public static ResponseData badRequest(String message) {
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .setBodyMessage(new JsonObject().put("message", message).encode());
        return responseData;
    }

    public static ResponseData internalServerError(String message) {
        ResponseData responseData = new ResponseData();
        responseData.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
                    .setBodyMessage(new JsonObject().put("message", message).encode());
        return responseData;
    }

}
