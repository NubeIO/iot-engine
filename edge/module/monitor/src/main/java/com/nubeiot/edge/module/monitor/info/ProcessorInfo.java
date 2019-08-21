package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.CentralProcessor;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonDeserialize(builder = ProcessorInfo.Builder.class)
public class ProcessorInfo implements JsonData {

    final String processor;
    final int physicalCpuPackageCount;
    final int physicalCpuCoreCount;
    final int logicalCpuCount;
    final String identifier;
    final String processorId;

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
