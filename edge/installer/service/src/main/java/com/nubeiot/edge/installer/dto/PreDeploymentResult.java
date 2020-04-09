package com.nubeiot.edge.installer.dto;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.auth.Credential;
import com.nubeiot.auth.Credential.HiddenCredential;
import com.nubeiot.core.IConfig;
import com.nubeiot.core.NubeConfig;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.IRequestData;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.State;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.utils.FileUtils;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.installer.InstallerConfig;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = PreDeploymentResult.Builder.class)
public final class PreDeploymentResult implements JsonData, IRequestData {

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
    @Setter
    @Default
    private boolean silent = false;

    public static JsonObject filterOutSensitiveConfig(String serviceId, JsonObject appCfg) {
        if ("com.nubeiot.edge.module:installer".equals(serviceId)) {
            logger.debug("Removing nexus password from result");
            AppConfig appConfig = IConfig.from(appCfg, AppConfig.class);
            Object installerObject = appConfig.get(InstallerConfig.NAME);
            if (Objects.isNull(installerObject)) {
                logger.debug("INSTALLER config is not available");
                return appCfg;
            }
            InstallerConfig installerConfig = IConfig.from(installerObject, InstallerConfig.class);
            installerConfig.getRepoConfig()
                           .getRemoteConfig()
                           .getUrls()
                           .values()
                           .forEach(remoteUrl -> remoteUrl.forEach(url -> {
                               Credential credential = url.getCredential();
                               if (Objects.isNull(credential)) {
                                   return;
                               }
                               url.setCredential(new HiddenCredential(credential));
                           }));
            appConfig.put(InstallerConfig.NAME, installerConfig);
            if (logger.isDebugEnabled()) {
                logger.debug("INSTALLER config {}", installerConfig.toJson());
            }
            return appConfig.toJson();
        }
        return appCfg;
    }

    public JsonObject toResponse() {
        JsonObject appConfig = filterOutSensitiveConfig(getServiceId(), getAppConfig().toJson());
        final JsonObject response = toJson();
        response.remove("silent");
        return response.put("app_config", appConfig).put("message", "Work in progress").put("status", Status.WIP);
    }

    @JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private JsonObject appConfig;
        private JsonObject systemConfig;
        private Path dataDir;

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

        public Builder dataDir(String dataDir) {
            this.dataDir = Strings.isBlank(dataDir) ? this.dataDir : FileUtils.toPath(dataDir);
            return this;
        }

        public PreDeploymentResult build() {
            AppConfig appConfig = IConfig.parseConfig(this.appConfig, AppConfig.class,
                                                      () -> IConfig.from(new JsonObject(), AppConfig.class));

            NubeConfig systemConfig = IConfig.parseConfig(this.systemConfig, NubeConfig.class,
                                                          () -> NubeConfig.blank(this.systemConfig));
            final Path dataDir = FileUtils.recomputeDataDir(
                Optional.ofNullable(this.dataDir).orElse(FileUtils.DEFAULT_DATADIR), FileUtils.normalize(serviceId));
            systemConfig.setDataDir(dataDir);
            return new PreDeploymentResult(transactionId, action, Objects.isNull(prevState) ? State.NONE : prevState,
                                           Objects.isNull(targetState) ? State.NONE : targetState, serviceId,
                                           serviceFQN, deployId, appConfig, systemConfig, silent);
        }

    }

}
