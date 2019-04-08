package com.nubeiot.core.http.handler;

import java.util.Map;
import java.util.Objects;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.http.base.HttpUtils;
import com.zandero.rest.writer.HttpResponseWriter;

public class ResponseDataWriter implements HttpResponseWriter<ResponseData> {

    public static ResponseData responseData(String message) {
        return new ResponseData().setBody(new JsonObject().put("message", message));
    }

    public static void responseData(ResponseData responseData, String message) {
        responseData.setBody(new JsonObject().put("message", message));
    }

    @Override
    public void write(ResponseData result, HttpServerRequest request, HttpServerResponse response) {
        String message = result.body().getString("message");
        response.setStatusCode(result.getStatus().code());
        response.putHeader(HttpHeaders.CONTENT_TYPE, HttpUtils.DEFAULT_CONTENT_TYPE);
        for (Map.Entry<String, Object> key : result.headers()) {
            response.putHeader(key.getKey(), key.getValue().toString());
        }
        if (Objects.isNull(message)) {
            response.end();
        } else {
            response.end(message);
        }
    }

}
