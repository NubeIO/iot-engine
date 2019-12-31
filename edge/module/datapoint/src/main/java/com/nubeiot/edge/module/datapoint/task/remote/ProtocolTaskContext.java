package com.nubeiot.edge.module.datapoint.task.remote;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.sql.EntityHandler;
import com.nubeiot.core.sql.workflow.task.EntityTaskContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public final class ProtocolTaskContext implements EntityTaskContext<EventbusClient> {

    @Getter
    private final EntityHandler entityHandler;

    @Override
    public EventbusClient transporter() {
        return entityHandler.eventClient();
    }

}
