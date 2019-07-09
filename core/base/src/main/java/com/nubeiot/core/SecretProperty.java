package com.nubeiot.core;

import java.io.Serializable;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecretProperty implements Serializable, JsonData {

    @Getter
    private final String ref;
    @Getter
    private final String value;


    @JsonCreator
    public SecretProperty(@NonNull @JsonProperty(value = "ref", required = true) String ref,
                          @NonNull @JsonProperty(value = "value", required = true) String value) {
        this.ref = ref;
        this.value = value;
    }

    public JsonObject serializeToJson() {
        return this.toJson().put("value", "******");
    }
}
