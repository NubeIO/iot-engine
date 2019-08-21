package com.nubeiot.edge.module.monitor.handlers;

import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.module.monitor.MonitorStatus;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import oshi.SystemInfo;

@NoArgsConstructor
public class MonitorStatusEventHandler implements EventListener {

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        SystemInfo si = new SystemInfo();
        MonitorStatus monitorStatus = MonitorStatus.from(si);
        return monitorStatus.toJson();
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
