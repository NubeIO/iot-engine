package com.nubeio.iot.edge;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeio.iot.share.enums.State;
import com.nubeio.iot.share.event.EventType;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PreDeploymentResult {

    private final String transactionId;
    private final EventType event;
    private final State prevState;
    private final String serviceId;
    private final String deployId;
    private final Map<String, Object> deployCfg;

    JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
