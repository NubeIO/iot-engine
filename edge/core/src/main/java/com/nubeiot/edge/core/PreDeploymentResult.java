package com.nubeiot.edge.core;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventType;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreDeploymentResult {

    private final String transactionId;
    private final EventType event;
    private final State prevState;
    private final String serviceId;
    private final String deployId;
    private final Map<String, Object> deployCfg;
    @Setter
    private boolean silent = false;

    // For Json
    private PreDeploymentResult() {
        this(null, null, null, null, null, null);
    }

    JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public JsonObject getDeployCfg() {
        return Objects.isNull(this.deployCfg) ? null : JsonObject.mapFrom(this.deployCfg);
    }

    public static PreDeploymentResult fromJson(JsonObject jsonObject) {
        return jsonObject.mapTo(PreDeploymentResult.class);
    }

}
