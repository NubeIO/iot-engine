package com.nubeiot.edge.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreDeploymentResult implements JsonData {

    private final String transactionId;
    private final EventAction action;
    private final State prevState;
    private final String serviceId;
    private final String deployId;
    private final Map<String, Object> deployCfg = new HashMap<>();
    @Setter
    private boolean silent;

    @JsonCreator
    public PreDeploymentResult(@JsonProperty(value = "transaction_id", required = true) String transactionId,
                               @JsonProperty(value = "action", required = true) EventAction action,
                               @JsonProperty(value = "prev_state") State prevState,
                               @JsonProperty(value = "service_id", required = true) String serviceId,
                               @JsonProperty(value = "deploy_id", required = true) String deployId,
                               @JsonProperty(value = "deploy_cfg") Map<String, Object> deployCfg) {
        this.transactionId = transactionId;
        this.action = action;
        this.prevState = Objects.isNull(prevState) ? State.NONE : prevState;
        this.serviceId = serviceId;
        this.deployId = deployId;
        if (Objects.nonNull(deployCfg)) {
            this.deployCfg.putAll(deployCfg);
        }
        this.silent = false;
    }

    public JsonObject getDeployCfg() {
        return JsonObject.mapFrom(this.deployCfg);
    }

    public static PreDeploymentResult from(JsonObject jsonObject) {
        return JsonData.from(jsonObject, PreDeploymentResult.class);
    }

}
