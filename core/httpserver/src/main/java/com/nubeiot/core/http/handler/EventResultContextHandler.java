package com.nubeiot.core.http.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventMessage;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.event.ReplyEventHandler;

public interface EventResultContextHandler extends Handler<RoutingContext> {

    EventController getController();

    default void sendAndListenEvent(RoutingContext context, String system, String address, EventPattern pattern,
                                    EventMessage message) {
        getController().request(address, pattern, message,
                                new ReplyEventHandler(system, message.getAction(), address, respMsg -> {
                                    context.put(EventAction.RETURN.name(), respMsg);
                                    context.next();
                                }, context::fail));
    }

}
