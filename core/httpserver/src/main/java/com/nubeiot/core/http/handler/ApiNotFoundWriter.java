package com.nubeiot.core.http.handler;

import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.CommonParamParser;
import com.zandero.rest.writer.NotFoundResponseWriter;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public final class ApiNotFoundWriter extends NotFoundResponseWriter {

    @Override
    public void write(HttpServerRequest request, HttpServerResponse response) {
        JsonObject result = new JsonObject().put("uri", request.absoluteURI()).put("message", "Resource not found");
        response.putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE)
                .end(CommonParamParser.prettify(result, request));
    }

}
