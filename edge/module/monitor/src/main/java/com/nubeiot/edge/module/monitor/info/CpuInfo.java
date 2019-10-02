package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class CpuInfo implements JsonData {

    private final String cpuLoad;
    private final String frequency;
    private final ProcessorInfo processor;
    private final CpuWiseProcesses cpuWiseProcesses;

    public static CpuInfo from(OperatingSystem os, CentralProcessor processor) {
        return CpuInfo.builder()
                      .cpuLoad(String.format("%.1f%% (OS MXBean)", processor.getSystemCpuLoad() * 100))
                      .frequency(FormatUtil.formatHertz(processor.getVendorFreq()))
                      .processor(ProcessorInfo.from(processor))
                      .cpuWiseProcesses(CpuWiseProcesses.from(os))
                      .build();
    }

}
