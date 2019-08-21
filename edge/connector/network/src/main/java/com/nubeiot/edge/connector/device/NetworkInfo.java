package com.nubeiot.edge.connector.device;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = NetworkInfo.Builder.class)
@RequiredArgsConstructor
public class NetworkInfo implements JsonData {

    private final String ipAddress;
    private final String subnetMask;
    private final String gateway;

    public static NetworkInfo from(JsonObject options) {
        return NetworkInfo.builder()
                          .ipAddress(options.getString("ip_address"))
                          .subnetMask(options.getString("subnet_mask"))
                          .gateway(options.getString("gateway"))
                          .build();
    }

}
