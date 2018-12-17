package com.nubeiot.core.http.handler;

import java.util.Objects;

import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.CommonParamParser;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.ext.web.RoutingContext;

public final class JsonContextHandler implements Handler<RoutingContext> {

    public static final String EVENT_RESULT = "event_result";

    @Override
    public void handle(RoutingContext context) {
        context.addHeadersEndHandler(
                v -> context.response().putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE));
        HttpMethod method = context.request().method();
        EventMessage eventMessage = context.get(EVENT_RESULT);
        if (Objects.isNull(eventMessage)) {
            context.next();
            return;
        }
        if (eventMessage.isSuccess()) {
            context.response()
                   .setStatusCode(HttpStatusMapping.success(method).code())
                   .end(CommonParamParser.prettify(eventMessage.getData(), context.request().getDelegate()));
        } else {
            context.response()
                   .setStatusCode(HttpStatusMapping.error(method, eventMessage.getError().getCode()).code())
                   .end(CommonParamParser.prettify(eventMessage.getError().toJson(), context.request().getDelegate()));
        }
    }

}
