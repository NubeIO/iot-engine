package com.nubeiot.edge.connector.datapoint.service;

import java.util.Collection;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.http.base.event.ActionMethodMapping;

import lombok.NonNull;

public class PointService implements EventListener {

    @Override
    public @NonNull Collection<EventAction> getAvailableEvents() {
        return ActionMethodMapping.CRUD_MAP.get().keySet();
    }

}
