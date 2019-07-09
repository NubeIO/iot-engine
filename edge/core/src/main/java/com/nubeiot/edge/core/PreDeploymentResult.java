package com.nubeiot.edge.core;

import java.nio.file.Path;
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
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.NubeConfig.SecretConfig;
import com.nubeiot.core.dto.IRequestData;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Builder.Default;
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
    private final State targetState;
    private final String serviceId;
    private final String serviceFQN;
    private final String deployId;
    private final AppConfig appConfig;
    private final NubeConfig systemConfig;
    private final SecretConfig secretConfig;
    private final String message;
    private final Status status;
    @Setter
    @Default
    private boolean silent = false;

    @Override
    public JsonObject toJson() {
        JsonObject output = toJson(mapper());
        output.remove("secret_config");
        return output;
    }


    @JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private JsonObject appConfig;
        private JsonObject systemConfig;
        private JsonObject secretConfig;
        private Path dataDir = FileUtils.DEFAULT_DATADIR;

        @JsonProperty("app_config")
        public Builder appConfig(Map<String, Object> appConfig) {
            return this.appConfig(JsonObject.mapFrom(appConfig));
        }

        public Builder appConfig(JsonObject appConfig) {
            this.appConfig = JsonObject.mapFrom(appConfig);
            return this;
        }

        @JsonProperty("system_config")
        public Builder systemConfig(Map<String, Object> systemConfig) {
            return this.systemConfig(JsonObject.mapFrom(systemConfig));
        }

        public Builder systemConfig(JsonObject systemConfig) {
            this.systemConfig = JsonObject.mapFrom(systemConfig);
            return this;
        }

        @JsonProperty("secret_config")
        public Builder secretConfig(Map<String, Object> secretConfig) {
            return this.secretConfig(JsonObject.mapFrom(secretConfig));
        }

        public Builder secretConfig(JsonObject secretConfig) {
            this.secretConfig = JsonObject.mapFrom(secretConfig);
            return this;
        }

        public Builder dataDir(String dataDir) {
            this.dataDir = Strings.isBlank(dataDir) ? this.dataDir : FileUtils.toPath(dataDir);
            return this;
        }

        public PreDeploymentResult build() {
            AppConfig appConfig = IConfig.parseConfig(this.appConfig, AppConfig.class,
                                                      () -> IConfig.from(new JsonObject(), AppConfig.class));
            SecretConfig secretConfig = IConfig.parseConfig(this.secretConfig, SecretConfig.class,
                                                            () -> IConfig.from(new JsonObject(), SecretConfig.class));
            NubeConfig systemConfig = IConfig.parseConfig(this.systemConfig, NubeConfig.class,
                                                          () -> NubeConfig.blank(this.systemConfig));
            systemConfig.setDataDir(FileUtils.recomputeDataDir(dataDir, FileUtils.normalize(serviceId)));

            return new PreDeploymentResult(transactionId, action, Objects.isNull(prevState) ? State.NONE : prevState,
                                           Objects.isNull(targetState) ? State.NONE : targetState, serviceId,
                                           serviceFQN, deployId, appConfig, systemConfig, secretConfig, message, status,
                                           silent);
        }

    }

}
