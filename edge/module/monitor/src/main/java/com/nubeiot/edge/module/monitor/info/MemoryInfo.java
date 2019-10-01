package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class MemoryInfo implements JsonData {

    private final String totalMemory;
    private final String availableMemory;
    private final String totalSwap;
    private final String swapUsed;
    private final MemoryWiseProcesses memoryWiseProcesses;

    public static MemoryInfo from(OperatingSystem os, GlobalMemory memory) {
        return MemoryInfo.builder()
                         .totalMemory(FormatUtil.formatBytes(memory.getTotal()))
                         .availableMemory(FormatUtil.formatBytes(memory.getAvailable()))
                         .totalSwap(FormatUtil.formatBytes(memory.getSwapTotal()))
                         .swapUsed(FormatUtil.formatBytes(memory.getSwapUsed()))
                         .memoryWiseProcesses(MemoryWiseProcesses.from(os, memory))
                         .build();
    }

}
