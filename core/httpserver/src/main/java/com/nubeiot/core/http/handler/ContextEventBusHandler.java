package com.nubeiot.core.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.web.RoutingContext;

public final class ContextEventBusHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(ContextEventBusHandler.class);

    @Override
    public void handle(RoutingContext successContext) {
    }

}
