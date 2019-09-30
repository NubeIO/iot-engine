package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
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
public class UptimeInfo implements JsonData {

    final String duration;

    public static UptimeInfo from(CentralProcessor processor) {
        return UptimeInfo.builder().duration(FormatUtil.formatElapsedSecs(processor.getSystemUptime())).build();
    }

}
