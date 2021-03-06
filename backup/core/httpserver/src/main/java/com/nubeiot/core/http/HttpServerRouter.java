package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.http.base.event.WebsocketServerEventMetadata;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.RestEventApi;

import lombok.Getter;

//TODO: Use Builder: WebsocketEventBuilder, RestEventApisBuilder
@Getter
public final class HttpServerRouter {

    private final Set<Class<? extends RestApi>> restApiClasses = new HashSet<>();
    private final Set<Class<? extends RestEventApi>> restEventApiClasses = new HashSet<>();
    private final Set<WebsocketServerEventMetadata> websocketEvents = new HashSet<>();
    private final Set<Class<? extends RestEventApi>> gatewayApiClasses = new HashSet<>();

    @SafeVarargs
    public final HttpServerRouter registerApi(Class<? extends RestApi>... apiClass) {
        restApiClasses.addAll(Arrays.stream(apiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final HttpServerRouter registerEventBusApi(Class<? extends RestEventApi>... eventBusApiClass) {
        return registerEventBusApi(Arrays.stream(eventBusApiClass).collect(Collectors.toList()));
    }

    public final HttpServerRouter registerEventBusApi(Collection<Class<? extends RestEventApi>> eventBusApiClass) {
        restEventApiClasses.addAll(eventBusApiClass.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final HttpServerRouter registerGatewayApi(Class<? extends RestEventApi>... gatewayApiClass) {
        gatewayApiClasses.addAll(Arrays.stream(gatewayApiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public HttpServerRouter registerEventBusSocket(WebsocketServerEventMetadata... eventBusSocket) {
        websocketEvents.addAll(Arrays.stream(eventBusSocket).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

}
