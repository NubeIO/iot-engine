package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import oshi.hardware.CentralProcessor;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class ProcessorInfo implements JsonData {

    private final String processor;
    private final int physicalCpuPackageCount;
    private final int physicalCpuCoreCount;
    private final int logicalCpuCount;
    private final String identifier;
    private final String processorId;

    public static ProcessorInfo from(CentralProcessor processor) {
        return ProcessorInfo.builder()
                            .processor(processor.toString())
                            .physicalCpuPackageCount(processor.getPhysicalPackageCount())
                            .physicalCpuCoreCount(processor.getPhysicalPackageCount())
                            .logicalCpuCount(processor.getLogicalProcessorCount())
                            .identifier(processor.getIdentifier())
                            .processorId(processor.getProcessorID())
                            .build();
    }

}
