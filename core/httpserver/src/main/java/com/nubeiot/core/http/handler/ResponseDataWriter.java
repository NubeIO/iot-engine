package com.nubeiot.core.http.handler;

import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;

import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.dto.ResponseData;
import com.zandero.rest.writer.HttpResponseWriter;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public class ResponseDataWriter implements HttpResponseWriter<ResponseData> {

    @Override
    public void write(ResponseData result, HttpServerRequest request, HttpServerResponse response) {
        String message = result.body().getString("message");
        response.setStatusCode(result.getStatus().code());
        response.putHeader(HttpHeaders.CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        for (Map.Entry<String, Object> key : result.headers()) {
            response.putHeader(key.getKey(), key.getValue().toString());
        }
        if (Objects.isNull(message)) {
            response.end();
        } else {
            response.end(message);
        }
    }

    public static ResponseData responseData(String message) {
        return new ResponseData().setBody(new JsonObject().put("message", message));
    }

    public static void responseData(ResponseData responseData, String message) {
        responseData.setBody(new JsonObject().put("message", message));
    }

}
