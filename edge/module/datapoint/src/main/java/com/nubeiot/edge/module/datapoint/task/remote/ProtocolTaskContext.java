package com.nubeiot.edge.module.datapoint.task.remote;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.workflow.task.EntityTaskContext;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ProtocolTaskContext implements EntityTaskContext<EventbusClient> {

    private final EntityHandler handler;

    @Override
    public @NonNull EntityHandler handler() {
        return handler;
    }

    @Override
    public EventbusClient transporter() {
        return getSharedDataValue(SharedDataDelegate.SHARED_EVENTBUS);
    }

}
