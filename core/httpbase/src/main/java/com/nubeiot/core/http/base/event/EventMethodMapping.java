package com.nubeiot.core.http.base.event;

import java.util.Objects;

import io.vertx.core.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder(builderClassName = "Builder")
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = EventMethodMapping.Builder.class)
public final class EventMethodMapping implements JsonData {

    @Include
    @NonNull
    private final EventAction action;
    @Include
    @NonNull
    private final HttpMethod method;
    private String capturePath;
    /**
     * Optional
     */
    private String regexPath;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        public EventMethodMapping build() {
            if (Objects.nonNull(capturePath) && Objects.isNull(regexPath)) {
                regexPath = EventMethodDefinition.toRegex(capturePath);
            }
            return new EventMethodMapping(action, method, capturePath, regexPath);
        }

    }

}
