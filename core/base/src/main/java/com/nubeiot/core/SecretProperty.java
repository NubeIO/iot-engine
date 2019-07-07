package com.nubeiot.core;

import java.io.Serializable;

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
    private final String display;
    @Getter
    private final String ref;
    @Getter
    private final String value;

    @JsonCreator
    private SecretProperty(@JsonProperty(value = "display", defaultValue = "******") String display,
                           @NonNull @JsonProperty(value = "ref", required = true) String ref,
                           @NonNull @JsonProperty(value = "value", required = true) String value) {
        this.display = display;
        this.ref = ref;
        this.value = value;
    }

    public SecretProperty(String ref, String value) {
        this(null, ref, value);
    }

}
