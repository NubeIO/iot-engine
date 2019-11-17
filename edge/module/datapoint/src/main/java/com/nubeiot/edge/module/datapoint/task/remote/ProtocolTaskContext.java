package com.nubeiot.edge.module.datapoint.task.remote;

import java.util.function.Function;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.service.task.EntityTaskContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ProtocolTaskContext implements EntityTaskContext<EventController> {

    private final EntityHandler handler;

    @Override
    public @NonNull EntityHandler handler() {
        return handler;
    }

    @Override
    public boolean isConcurrent() {
        return true;
    }

    @Override
    public ProtocolTaskContext registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

    @Override
    public EventController transporter() {
        return getSharedDataValue(SharedDataDelegate.SHARED_EVENTBUS);
    }

}
