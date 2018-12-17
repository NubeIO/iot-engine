package com.nubeiot.core.http.handler;

import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.CommonParamParser;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

public final class NotFoundContextHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext event) {
        HttpServerRequest request = event.request();
        JsonObject result = new JsonObject().put("uri", request.absoluteURI()).put("message", "Resource not found");
        event.response()
             .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
             .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE)
             .end(CommonParamParser.prettify(result, request.getDelegate()));
    }

}
