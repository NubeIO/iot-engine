package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.GlobalMemory;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MemoryInfo implements JsonData {

    final String totalMemory;
    final String availableMemory;
    final String totalSwap;
    final String swapUsed;

    public static MemoryInfo from(GlobalMemory memory) {
        return MemoryInfo.builder()
                         .totalMemory(FormatUtil.formatBytes(memory.getTotal()))
                         .availableMemory(FormatUtil.formatBytes(memory.getAvailable()))
                         .totalSwap(FormatUtil.formatBytes(memory.getSwapTotal()))
                         .swapUsed(FormatUtil.formatBytes(memory.getSwapUsed()))
                         .build();
    }

}
