package com.nubeiot.edge.module.monitor.handlers;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.module.monitor.MonitorService;
import com.nubeiot.edge.module.monitor.info.UptimeInfo;

import lombok.NoArgsConstructor;
import oshi.SystemInfo;

@NoArgsConstructor
public class MonitorUptimeService implements MonitorService {

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        SystemInfo si = new SystemInfo();
        return UptimeInfo.from(si.getHardware().getProcessor()).toJson();
    }

    @Override
    public String servicePath() {
        return "/uptime";
    }

}
