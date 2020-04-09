package com.nubeiot.edge.installer.dto;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = PostDeploymentResult.Builder.class)
public final class PostDeploymentResult implements JsonData {

    private String serviceId;
    private String transactionId;
    private String deployId;
    private String historyId;
    private EventAction action;
    private State prevState;
    private State toState;
    private State toActualState;
    private Status status;
    private JsonObject error;
    private int affectRecords;

    public static PostDeploymentResult from(@NonNull PreDeploymentResult pre, String deployId, JsonObject error) {
        return PostDeploymentResult.builder()
                                   .serviceId(pre.getServiceId())
                                   .transactionId(pre.getTransactionId())
                                   .deployId(deployId)
                                   .prevState(pre.getPrevState())
                                   .toState(pre.getTargetState())
                                   .action(pre.getAction())
                                   .error(error)
                                   .status(Objects.isNull(error) || error.isEmpty() ? Status.SUCCESS : Status.FAILED)
                                   .build();
    }

    public static PostDeploymentResult from(@NonNull PostDeploymentResult result, State finalState, int affectRecords) {
        return PostDeploymentResult.builder()
                                   .serviceId(result.getServiceId())
                                   .transactionId(result.getTransactionId())
                                   .deployId(result.getDeployId())
                                   .action(result.getAction())
                                   .error(result.getError())
                                   .status(result.getStatus())
                                   .prevState(result.getPrevState())
                                   .toState(result.getToState())
                                   .toActualState(finalState)
                                   .affectRecords(affectRecords)
                                   .build();
    }

    @JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
