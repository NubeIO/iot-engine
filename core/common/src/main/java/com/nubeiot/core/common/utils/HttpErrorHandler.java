package com.nubeiot.core.common.utils;

import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE;
import static com.nubeiot.core.common.utils.response.ResponseUtils.CONTENT_TYPE_JSON;

import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Tobe removed
 */
@Deprecated
public class HttpErrorHandler {
    public static void handleError(Throwable throwable, RoutingContext routingContext) {
        HttpException exception = (HttpException) throwable;
        routingContext.response()
            .setStatusCode(exception.getStatusCode().code())
            .putHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
            .end(throwable.getMessage());
    }
}
