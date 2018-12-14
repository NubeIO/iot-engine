package com.nubeiot.core.http.handler;

import java.util.Objects;

import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.CommonParamParser;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.RoutingContext;

public final class ContextFailureHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(ContextFailureHandler.class);

    @Override
    public void handle(RoutingContext failureContext) {
        final HttpMethod method = failureContext.request().method();
        final Throwable throwable = failureContext.failure();
        if (Objects.nonNull(throwable)) {
            logger.error("API exception", throwable);
            ErrorMessage errorMessage = ErrorMessage.parse(throwable);
            failureContext.response()
                          .putHeader(ApiConstants.CONTENT_TYPE, ApiConstants.DEFAULT_CONTENT_TYPE)
                          .setStatusCode(HttpStatusMapping.error(method, errorMessage.getThrowable()).code())
                          .end(CommonParamParser.prettify(errorMessage, failureContext.request().getDelegate()));
        }
    }

}
