package com.nubeiot.edge.core;

import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.NubeConfig.AppConfig;
import com.nubeiot.core.dto.JsonData;

import lombok.Getter;

public class RequestedServiceData implements JsonData {

    @Getter
    private final JsonObject metadata;
    @Getter
    private final AppConfig appConfig;

    public RequestedServiceData() {
        this.metadata = new JsonObject();
        this.appConfig = new AppConfig();
    }

    @JsonCreator
    public RequestedServiceData(@JsonProperty(value = "metadata") Map<String, Object> metadata,
                                @JsonProperty(value = "appConfig") AppConfig appConfig) {
        this.metadata = Objects.isNull(metadata) ? new JsonObject() : new JsonObject(metadata);
        this.appConfig = Objects.isNull(appConfig) ? new AppConfig() : appConfig;
    }

}
