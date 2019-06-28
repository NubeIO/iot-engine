package com.nubeiot.dashboard.connector.sample.kafka;

import java.util.Collection;
import java.util.Collections;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;

import lombok.NonNull;

public class EnableKafkaEventListener implements EventListener {

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.UPDATE);
    }

    @EventContractor(action = EventAction.UPDATE, returnType = String.class)
    public String update() {
        return "success";
    }

}
