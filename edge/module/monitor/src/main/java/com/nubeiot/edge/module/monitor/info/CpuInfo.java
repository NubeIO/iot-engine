package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CpuInfo implements JsonData {

    final String cpuLoad;
    final String frequency;
    final ProcessorInfo processor;
    final CpuWiseProcesses cpuWiseProcesses;

    public static CpuInfo from(OperatingSystem os, CentralProcessor processor) {
        return CpuInfo.builder()
                      .cpuLoad(String.format("%.1f%% (OS MXBean)", processor.getSystemCpuLoad() * 100))
                      .frequency(FormatUtil.formatHertz(processor.getVendorFreq()))
                      .processor(ProcessorInfo.from(processor))
                      .cpuWiseProcesses(CpuWiseProcesses.from(os))
                      .build();
    }

}
