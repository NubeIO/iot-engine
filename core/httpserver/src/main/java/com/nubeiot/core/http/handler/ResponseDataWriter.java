package com.nubeiot.core.http.handler;

import java.util.Objects;

import com.nubeiot.core.dto.ResponseData;
import com.zandero.rest.writer.HttpResponseWriter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class ResponseDataWriter implements HttpResponseWriter<ResponseData> {

    @Override
    public void write(ResponseData result, HttpServerRequest request, HttpServerResponse response) {
        String message = result.getBody().getString("message");
        response.setStatusCode(result.getHeaders().getInteger("statusCode"));
        if (Objects.isNull(message)) {
            response.end();
        } else {
            response.end(message);
        }
    }

}
