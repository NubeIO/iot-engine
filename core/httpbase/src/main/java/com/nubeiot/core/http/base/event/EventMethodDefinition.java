package com.nubeiot.core.http.base.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.http.base.HttpUtils.HttpMethods;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;

/**
 * It helps define a mapping between dynamic route by {@code regex path} and {@code HttpMethod} with {@code
 * EventAction}
 */
public final class EventMethodDefinition implements JsonData {

    @JsonUnwrapped
    private final Map<String, Set<EventMethodMapping>> eventPaths = new HashMap<>();

    /**
     * Create default definition with default {@link ActionMethodMapping#defaultEventHttpMap()}.
     *
     * @param servicePath       Origin service path that represents for manipulating {@code resource} in all given
     *                          {@code HTTPMethod}
     * @param singularRegexPath Singular actual regex path to distinguish path for manipulating {@code single resource}
     *                          or {@code resource list}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping#defaultEventHttpMap()
     * @see HttpMethods#isSingular(HttpMethod)
     */
    public static EventMethodDefinition createDefault(String servicePath, String singularRegexPath) {
        return create(servicePath, singularRegexPath, ActionMethodMapping.defaultEventHttpMap());
    }

    public static EventMethodDefinition create(String servicePath, String singularRegexPath,
                                               @NonNull ActionMethodMapping mapping) {
        return create(servicePath, singularRegexPath, mapping.get());
    }

    private static EventMethodDefinition create(String servicePath, String singularRegexPath,
                                                @NonNull Map<EventAction, HttpMethod> actionMethodMap) {
        Strings.requireNotBlank(servicePath);
        Strings.requireNotBlank(singularRegexPath);
        EventMethodDefinition definition = new EventMethodDefinition();
        actionMethodMap.forEach((action, method) -> {
            String regex = null;
            if (action == EventAction.GET_ONE || (HttpMethods.isSingular(method) && action != EventAction.GET_LIST)) {
                regex = singularRegexPath;
            }
            definition.add(servicePath,
                           EventMethodMapping.builder().action(action).method(method).regexPath(regex).build());
        });
        return definition;
    }

    public EventMethodDefinition add(String servicePath, @NonNull EventMethodMapping mapping) {
        this.eventPaths.computeIfAbsent(Strings.requireNotBlank(servicePath), s -> new HashSet<>()).add(mapping);
        return this;
    }

    public EventAction search(String actualPath, @NonNull HttpMethod method) {
        String path = Strings.requireNotBlank(actualPath);
        return eventPaths.entrySet()
                         .stream()
                         .filter(entry -> path.startsWith(entry.getKey()))
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
    @Builder(builderClassName = "Builder")
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(builder = EventMethodMapping.Builder.class)
    //TODO make it works with `capture_path`
    public final static class EventMethodMapping {

        @Include
        @NonNull
        private final EventAction action;
        @Include
        @NonNull
        private final HttpMethod method;
        /**
         * Optional
         */
        private String regexPath;


        @JsonPOJOBuilder(withPrefix = "")
        public static class Builder {}

    }

}
