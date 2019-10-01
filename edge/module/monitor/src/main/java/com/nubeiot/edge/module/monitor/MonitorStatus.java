package com.nubeiot.edge.module.monitor;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.edge.module.monitor.info.CpuInfo;
import com.nubeiot.edge.module.monitor.info.FileSystemInfos;
import com.nubeiot.edge.module.monitor.info.MemoryInfo;
import com.nubeiot.edge.module.monitor.info.OsInfo;
import com.nubeiot.edge.module.monitor.info.UptimeInfo;

import lombok.Builder;
import lombok.Getter;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class MonitorStatus implements JsonData {

    private final CpuInfo cpuUtilization;
    private final MemoryInfo memoryUtilization;
    private final FileSystemInfos diskUtilization;
    private final UptimeInfo uptime;
    private final OsInfo os;

    public static MonitorStatus from(SystemInfo si) {
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        return MonitorStatus.builder()
                            .cpuUtilization(CpuInfo.from(os, hal.getProcessor()))
                            .memoryUtilization(MemoryInfo.from(os, hal.getMemory()))
                            .diskUtilization(FileSystemInfos.from(os.getFileSystem()))
                            .uptime(UptimeInfo.from(hal.getProcessor()))
                            .os(OsInfo.from(os))
                            .build();
    }

}
