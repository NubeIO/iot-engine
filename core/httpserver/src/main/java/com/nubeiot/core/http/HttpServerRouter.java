package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

@Getter
public final class HttpServerRouter {

    private final Set<Class> restApiClass = new HashSet<>();
    private final Set<Class<? extends RestEventApi>> restEventApiClass = new HashSet<>();
    private final Set<WebsocketEventMetadata> websocketEvents = new HashSet<>();

    public HttpServerRouter registerApi(Class... apiClass) {
        restApiClass.addAll(Arrays.stream(apiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpServerRouter registerEventBusApi(Class<? extends RestEventApi>... eventBusApiClass) {
        restEventApiClass.addAll(Arrays.stream(eventBusApiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public HttpServerRouter registerEventBusSocket(WebsocketEventMetadata... eventBusSocket) {
        websocketEvents.addAll(Arrays.stream(eventBusSocket).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    boolean hasRestApi() {
        return hasApi() || hasEventBusApi();
    }

    boolean hasApi() {
        return !restApiClass.isEmpty();
    }

    boolean hasEventBusApi() {
        return !restEventApiClass.isEmpty();
    }

}
