package com.nubeiot.core.http.handler;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.ReplyEventHandler;
import com.nubeiot.core.http.rest.RestEventMetadata;
import com.nubeiot.core.http.utils.RequestConverter;

import lombok.RequiredArgsConstructor;

/**
 * Represents for pushing data via {@code Eventbus} then listen {@code reply message}. After receiving {@code reply
 * message}, redirect it to {@code next Context handler}
 *
 * @see RestEventResponseHandler
 */
@RequiredArgsConstructor
public class RestEventResultHandler implements Handler<RoutingContext> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestEventMetadata metadata;

    @Override
    public void handle(RoutingContext context) {
        EventMessage msg = EventMessage.success(metadata.getAction(), RequestConverter.convert(context));
        logger.info("REST::Request data: {}", msg.toJson().encode());
        ReplyEventHandler replyEventHandler = new ReplyEventHandler("REST", metadata.getAction(), metadata.getAddress(),
                                                                    message -> response(context, message),
                                                                    context::fail);
        new EventController(context.vertx()).request(metadata.getAddress(), metadata.getPattern(), msg,
                                                     replyEventHandler);
    }

    private void response(RoutingContext context, EventMessage message) {
        context.put(EventAction.RETURN.name(), message);
        context.next();
    }

}
