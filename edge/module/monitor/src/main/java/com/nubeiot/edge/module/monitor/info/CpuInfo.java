package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.CentralProcessor;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonDeserialize(builder = CpuInfo.Builder.class)
public class CpuInfo implements JsonData {

    final String uptime;
    final String cpuLoad;
    final String frequency;

    public static CpuInfo from(CentralProcessor processor) {
        return CpuInfo.builder()
                      .uptime(FormatUtil.formatElapsedSecs(processor.getSystemUptime()))
                      .cpuLoad(String.format("%.1f%% (OS MXBean)", processor.getSystemCpuLoad() * 100))
                      .frequency(FormatUtil.formatHertz(processor.getVendorFreq()))
                      .build();
    }

}
