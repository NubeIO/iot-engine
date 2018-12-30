package com.nubeiot.core.http.handler;

import java.util.Objects;

import com.nubeiot.core.http.CommonParamParser;
import com.zandero.rest.writer.HttpResponseWriter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public final class ApiJsonWriter<T> implements HttpResponseWriter<T> {

    @Override
    public void write(T result, HttpServerRequest request, HttpServerResponse response) {
        if (Objects.isNull(result)) {
            response.end();
        } else {
            response.end(CommonParamParser.prettify(result, request));
        }
    }

}
