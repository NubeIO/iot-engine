package com.nubeiot.core.http.handler;

import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.dto.ResponseData;
import com.zandero.rest.writer.HttpResponseWriter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class ResponseDataWriter implements HttpResponseWriter<ResponseData> {

    @Override
    public void write(ResponseData result, HttpServerRequest request, HttpServerResponse response) {
        String message = result.getBodyMessage();
        response.setStatusCode(result.getStatusCode());
        for (Map.Entry<String, Object> key : result.getHeaders()) {
            response.putHeader(key.getKey(), key.getValue().toString());
        }
        if (Objects.isNull(message)) {
            response.end();
        } else {
            response.end(message);
        }
    }

}
