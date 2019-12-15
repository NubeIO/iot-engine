package com.nubeiot.edge.connector.bacnet.handler;

import java.util.Collection;
import java.util.Collections;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

import lombok.NonNull;

public class DiscoverCompletionHandler implements EventListener {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singleton(EventAction.NOTIFY);
    }

    @EventContractor(action = EventAction.NOTIFY, returnType = boolean.class)
    public boolean receive(RequestData requestData) {
        logger.info(requestData.toJson());
        return true;
    }

}
