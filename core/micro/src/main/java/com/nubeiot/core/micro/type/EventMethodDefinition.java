package com.nubeiot.core.micro.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;

public final class EventMethodDefinition implements JsonData {

    @JsonUnwrapped
    private final Map<String, Set<EventMethodMapping>> regexPaths = new HashMap<>();

    public EventMethodDefinition add(String regexPath, EventMethodMapping mapping) {
        this.regexPaths.computeIfAbsent(regexPath, s -> new HashSet<>()).add(mapping);
        return this;
    }

    public EventAction search(String actualPath, @NonNull HttpMethod method) {
        String path = Strings.requireNotBlank(actualPath);
        return regexPaths.entrySet()
                         .stream()
                         .filter(entry -> path.matches(entry.getKey()))
                         .map(entry -> entry.getValue()
                                            .stream()
                                            .filter(mapping -> mapping.method == method &&
                                                               (Strings.isBlank(mapping.regexPath) ||
                                                                path.matches(mapping.regexPath)))
                                            .findFirst())
                         .map(m -> m.orElseThrow(() -> new NotFoundException(
                             Strings.format("Not found '{0}' with HTTP method {1}", actualPath, method))).action)
                         .findFirst()
                         .orElseThrow(() -> new NotFoundException("Not found path " + actualPath));
    }

    @Getter
    @Builder
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public final static class EventMethodMapping {

        @Include
        private final EventAction action;
        @Include
        private final HttpMethod method;
        /**
         * Optional
         */
        private String regexPath;

    }

}
