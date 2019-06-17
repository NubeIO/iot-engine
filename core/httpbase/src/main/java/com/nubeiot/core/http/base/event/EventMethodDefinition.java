package com.nubeiot.core.http.base.event;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.http.base.HttpUtils.HttpMethods;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * It helps define a mapping between dynamic route by {@code regex path} and {@code HttpMethod} with {@code EventAction}
 * that used by specific {@code EventBus address}
 */
@Getter
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(builder = EventMethodDefinition.Builder.class)
public final class EventMethodDefinition implements JsonData {

    private static final Logger logger = LoggerFactory.getLogger(EventMethodDefinition.class);

    @EqualsAndHashCode.Include
    private final String servicePath;
    /**
     * Web Router order
     */
    @JsonIgnore
    private final int order;
    private final Set<EventMethodMapping> mapping;
    /**
     * Identify using {@link RequestData} or not. Default is {@code True}
     * <p>
     * If {@code False}, only {@code path params} and {@code body} in {@code HTTP Request} will be included and omit
     * data in {@code HTTP Request query params} and {@code HTTP Request Header}
     */
    private final boolean useRequestData;

    private EventMethodDefinition(String servicePath, boolean useRequestData,
                                  @NonNull Set<EventMethodMapping> mapping) {
        this.servicePath = toRegex(Strings.requireNotBlank(servicePath));
        if (this.servicePath.endsWith("/.+")) {
            throw new IllegalArgumentException("Service path cannot ends with capture parameter");
        }
        this.useRequestData = useRequestData;
        if (!useRequestData) {
            logger.warn("HTTP Path '{}' is not using `RequestData` that will omit data in `HTTP Request Query` and " +
                        "`HTTP Request Header`", this.servicePath);
        }
        this.order = Networks.priorityOrder(this.servicePath.length());
        this.mapping = mapping;
    }

    static String toRegex(String capturePath) {
        return capturePath.replaceFirst("/:[^/]+(/?)$", "/.+$1").replaceAll("/:[^/]+", "/[^/]+");
    }

    private static String searchRegex(String servicePath) {
        return servicePath + (servicePath.endsWith("/") ? "(.+)?" : "(/.+)?");
    }

    /**
     * Create default definition with default {@link ActionMethodMapping#DEFAULT}.
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} in all given {@code
     *                    HTTPMethod}
     * @param capturePath Capturing path parameters for manipulating {@code resource}. E.g: {@code
     *                    /catalogue/products/:productType/:productId/}
     * @return new instance of {@link EventMethodDefinition}
     * @see #createDefault(String, String, boolean)
     */
    public static EventMethodDefinition createDefault(String servicePath, String capturePath) {
        return createDefault(servicePath, capturePath, true);
    }

    /**
     * Create default definition with default {@link ActionMethodMapping#DEFAULT}.
     *
     * @param servicePath    Origin service path that represents for manipulating {@code resource} in all given {@code
     *                       HTTPMethod}
     * @param capturePath    Capturing path parameters for manipulating {@code resource}. E.g: {@code
     *                       /catalogue/products/:productType/:productId/}
     * @param useRequestData Whether use {@link RequestData} in parameter in {@link EventHandler} or not
     * @return new instance of {@link EventMethodDefinition}
     */
    public static EventMethodDefinition createDefault(String servicePath, String capturePath, boolean useRequestData) {
        return create(servicePath, Strings.requireNotBlank(capturePath), ActionMethodMapping.DEFAULT, useRequestData);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code singleton resource} with no key or {@code action job}, e.g: {@code translate}
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} or {@code action job}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, @NonNull ActionMethodMapping mapping) {
        return create(servicePath, mapping, true);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code singleton resource} with no key or {@code action job}, e.g: {@code translate}
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource} or {@code action job}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, @NonNull ActionMethodMapping mapping,
                                               boolean useRequestData) {
        return create(servicePath, null, mapping, useRequestData);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code resource} with common {@code CRUD} operations
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource}
     * @param capturePath Capturing path parameters for manipulating {@code resource}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, String capturePath,
                                               @NonNull ActionMethodMapping mapping) {
        return create(servicePath, capturePath, mapping, true);
    }

    /**
     * Create definition with given {@code ActionMethodMapping}.
     * <p>
     * It is appropriate to handle {@code resource} with common {@code CRUD} operations
     *
     * @param servicePath Origin service path that represents for manipulating {@code resource}
     * @param capturePath Capturing path parameters for manipulating {@code resource}
     * @param mapping     Mapping between {@code EventAction} and {@code HTTPMethod}
     * @return new instance of {@link EventMethodDefinition}
     * @see ActionMethodMapping
     */
    public static EventMethodDefinition create(String servicePath, String capturePath,
                                               @NonNull ActionMethodMapping mapping, boolean useRequestData) {
        if (Strings.isBlank(capturePath) && mapping.hasDuplicateMethod()) {
            throw new IllegalStateException("Has duplicate HTTP method for same endpoint");
        }
        Set<EventMethodMapping> map = new HashSet<>();
        mapping.get().forEach((action, method) -> {
            String path = servicePath;
            if (action == EventAction.GET_ONE || (HttpMethods.isSingular(method) && action != EventAction.GET_LIST)) {
                path = capturePath;
            }
            map.add(EventMethodMapping.builder().action(action).method(method).capturePath(path).build());
        });
        return EventMethodDefinition.builder()
                                    .servicePath(servicePath)
                                    .useRequestData(useRequestData)
                                    .mapping(map)
                                    .build();
    }

    public EventAction search(String actualPath, @NonNull HttpMethod method) {
        final String path = Strings.requireNotBlank(actualPath);
        if (!path.matches(searchRegex(this.servicePath))) {
            throw new NotFoundException("Not found path " + actualPath);
        }
        return mapping.stream()
                      .filter(mapping -> {
                          String regex = Strings.isBlank(mapping.getRegexPath()) ? servicePath : mapping.getRegexPath();
                          return mapping.getMethod() == method && path.matches(regex);
                      })
                      .map(EventMethodMapping::getAction)
                      .findFirst()
                      .orElseThrow(() -> new NotFoundException(
                          Strings.format("Not found ''{0}'' with HTTP method {1}", actualPath, method)));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        Boolean useRequestData = true;

        public EventMethodDefinition build() {
            return new EventMethodDefinition(this.servicePath, this.useRequestData, this.mapping);
        }

    }

}
