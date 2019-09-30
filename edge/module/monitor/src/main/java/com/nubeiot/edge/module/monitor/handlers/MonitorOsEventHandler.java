package com.nubeiot.edge.module.monitor.handlers;

import java.util.Collections;
import java.util.List;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.edge.module.monitor.info.OsInfo;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import oshi.SystemInfo;

@NoArgsConstructor
public class MonitorOsEventHandler implements EventListener {

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        SystemInfo si = new SystemInfo();
        return OsInfo.from(si.getOperatingSystem()).toJson();
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() {
        return Collections.singletonList(EventAction.GET_LIST);
    }

}
