package com.nubeiot.edge.module.monitor.info;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.software.os.OperatingSystem;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OsInfo implements JsonData {

    final String name;

    public static OsInfo from(OperatingSystem os) {
        return OsInfo.builder().name(os.toString()).build();
    }

}

