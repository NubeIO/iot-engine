package com.nubeiot.core.dto;

import java.io.Serializable;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface DataTransferObject extends Serializable, JsonData {

    @JsonProperty(value = "body")
    JsonObject body();

    @JsonProperty(value = "headers")
    JsonObject headers();

}
