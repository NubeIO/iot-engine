package com.nubeiot.edge.module.monitor.service;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.edge.module.monitor.info.FileSystemInfos;

import lombok.NoArgsConstructor;
import oshi.SystemInfo;

@NoArgsConstructor
public class MonitorDiskUtilizationService implements MonitorService {

    @EventContractor(action = EventAction.GET_LIST)
    public JsonObject getList(RequestData data) {
        SystemInfo si = new SystemInfo();
        return FileSystemInfos.from(si.getOperatingSystem().getFileSystem()).toJson();
    }

    @Override
    public String servicePath() {
        return "/disk-utilization";
    }

}
