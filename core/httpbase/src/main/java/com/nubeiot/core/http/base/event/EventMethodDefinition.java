package com.nubeiot.core.http.base.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.http.HttpMethod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import lombok.ToString;

/**
 * It helps define a mapping between dynamic route by {@code regex path} and {@code HttpMethod} with {@code EventAction}
 * that used by specific {@code EventBus address}
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventMethodDefinition implements JsonData {

    @EqualsAndHashCode.Include
    private final String servicePath;
    private Set<EventMethodMapping> mapping = new HashSet<>();

    @JsonCreator
    public EventMethodDefinition(@JsonProperty(value = "servicePath") String servicePath) {
        this.servicePath = toRegex(Strings.requireNotBlank(servicePath));
        if (this.servicePath.endsWith("/.+")) {
            throw new IllegalArgumentException("Service path cannot ends with capture parameter");
        }
    }

    private static String toRegex(String capturePath) {
        return capturePath.replaceFirst("/:[^/]+(/?)$", "/.+$1").replaceAll("/:[^/]+", "/[^/]+");
    }

    private static String searchRegex(String servicePath) {
        return servicePath + (servicePath.endsWith("/") ? "(.+)?" : "(/.+)?");
    }

    /**
     * Create default definition with default {@link ActionMethodMapping#defaultEventHttpMap()}.
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} in all given {@code
     *                    HTTPMethod}
     * @param capturePath Capturing path parameters for manipulating single resource. E.g: {@code
     *                    /catalogue/products/:productType/:productId/}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping#defaultEventHttpMap()
     * @see HttpMethods#isSingular(HttpMethod)
     */
    public static EventMethodDefinition createDefault(String servicePath, String capturePath) {
        return create(servicePath, capturePath, ActionMethodMapping.defaultEventHttpMap());
    }

    /**
     * Create default definition with given {@code ActionMethodMapping}.
     *
     * @see #createDefault(String, String)
     */
    public static EventMethodDefinition create(String servicePath, String capturePath,
                                               @NonNull ActionMethodMapping mapping) {
        return create(servicePath, capturePath, mapping.get());
    }

    private static EventMethodDefinition create(String servicePath, String capturePath,
                                                @NonNull Map<EventAction, HttpMethod> actionMethodMap) {
        String patternPath = actionMethodMap.size() > 1 ? Strings.requireNotBlank(capturePath) : capturePath;
        EventMethodDefinition definition = new EventMethodDefinition(servicePath);
        actionMethodMap.forEach((action, method) -> {
            String path = servicePath;
            if (action == EventAction.GET_ONE || (HttpMethods.isSingular(method) && action != EventAction.GET_LIST)) {
                path = patternPath;
            }
            definition.add(EventMethodMapping.builder().action(action).method(method).capturePath(path).build());
        });
        return definition;
    }

    public EventMethodDefinition add(EventMethodMapping mapping) {
        this.mapping.add(mapping);
        return this;
    }

    public EventAction search(String actualPath, @NonNull HttpMethod method) {
        final String path = Strings.requireNotBlank(actualPath);
        if (!path.matches(searchRegex(this.servicePath))) {
            throw new NotFoundException("Not found path " + actualPath);
        }
        return mapping.stream()
                      .filter(mapping -> {
                          String regex = Strings.isBlank(mapping.regexPath) ? servicePath : mapping.regexPath;
                          return mapping.method == method && path.matches(regex);
                      })
                      .map(EventMethodMapping::getAction)
                      .findFirst()
                      .orElseThrow(() -> new NotFoundException(
                          Strings.format("Not found ''{0}'' with HTTP method {1}", actualPath, method)));
    }

    @Getter
    @Builder(builderClassName = "Builder")
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(builder = EventMethodMapping.Builder.class)
    public final static class EventMethodMapping implements JsonData {

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
                    regexPath = toRegex(capturePath);
                }
                return new EventMethodMapping(action, method, capturePath, regexPath);
            }

        }

    }

}
