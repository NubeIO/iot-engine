package com.nubeiot.core.http.handler;

import static com.nubeiot.core.http.ApiConstants.CONTENT_TYPE;
import static com.nubeiot.core.http.ApiConstants.DEFAULT_CONTENT_TYPE;

import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.dto.ResponseData;
import com.zandero.rest.writer.HttpResponseWriter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class ResponseDataWriter implements HttpResponseWriter<ResponseData> {

    @Override
    public void write(ResponseData result, HttpServerRequest request, HttpServerResponse response) {
        String message = result.body().getString("message");
        response.setStatusCode(result.statusCode());
        response.putHeader(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
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
