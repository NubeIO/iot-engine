package com.nubeiot.edge.core;

import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.dto.IRequestData;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NubeException;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = PreDeploymentResult.Builder.class)
public class PreDeploymentResult implements JsonData, IRequestData {

    private static final Logger logger = LoggerFactory.getLogger(PreDeploymentResult.class);

    private final String transactionId;
    private final EventAction action;
    private final State prevState;
    private final String serviceId;
    private final String deployId;
    private final NubeConfig deployCfg;
    @Setter
    @lombok.Builder.Default
    private boolean silent = false;


    @JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private JsonObject deployCfg;

        @JsonProperty("deploy_cfg")
        public Builder deployCfg(Map<String, Object> deployCfg) {
            return this.deployCfg(JsonObject.mapFrom(deployCfg));
        }

        public Builder deployCfg(JsonObject deployCfg) {
            this.deployCfg = JsonObject.mapFrom(deployCfg);
            return this;
        }

        public PreDeploymentResult build() {
            return new PreDeploymentResult(transactionId, action, Objects.isNull(prevState) ? State.NONE : prevState,
                                           serviceId, deployId, parseDeployCfg(deployCfg), silent);
        }

        private NubeConfig parseDeployCfg(JsonObject deployCfg) {
            try {
                return Objects.nonNull(deployCfg) ? IConfig.from(deployCfg, NubeConfig.class) : NubeConfig.blank();
            } catch (NubeException ex) {
                logger.trace("Try to parse deploy_cfg to AppConfig", ex);
                return NubeConfig.blank(deployCfg);
            }
        }

    }

}
