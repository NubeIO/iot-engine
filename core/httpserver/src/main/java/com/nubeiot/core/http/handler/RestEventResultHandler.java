package com.nubeiot.core.http.handler;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.utils.RequestDataConverter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents for pushing data via {@code Eventbus} then listen {@code reply message}. After receiving {@code reply
 * message}, redirect it to {@code next Context handler}
 *
 * @see RestEventResponseHandler
 */
@RequiredArgsConstructor
public class RestEventResultHandler implements EventResultContextHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestEventApiMetadata metadata;
    @Getter
    private EventController controller;

    @Override
    public void handle(RoutingContext context) {
        this.controller = new EventController(context.vertx());
        EventMessage msg = EventMessage.initial(metadata.getAction(), RequestDataConverter.convert(context));
        logger.info("REST::Request data: {}", msg.toJson().encode());
        sendAndListenEvent(context, "REST", metadata.getAddress(), metadata.getPattern(), msg);
    }

}
