package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.Collections;
import java.util.List;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;

import lombok.NonNull;

public class EnableKafkaEventHandler implements EventHandler {

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = String.class)
    public String update() {
        return "success";
    }

}
