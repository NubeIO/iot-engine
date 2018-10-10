package io.nubespark.utils;

import io.vertx.reactivex.ext.web.RoutingContext;

import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE;
import static io.nubespark.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

public class HttpErrorHandler {
    public static void handleError(Throwable throwable, RoutingContext routingContext) {
        HttpException exception = (HttpException) throwable;
        routingContext.response()
            .setStatusCode(exception.getStatusCode().code())
            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
            .end(throwable.getMessage());
    }
}
