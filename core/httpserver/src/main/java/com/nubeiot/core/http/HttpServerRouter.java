package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.http.rest.DynamicRestApi;
import com.nubeiot.core.http.rest.RestApi;
import com.nubeiot.core.http.rest.RestEventApi;
import com.nubeiot.core.http.ws.WebsocketEventMetadata;

import lombok.Getter;

//TODO: Use Builder: WebsocketEventBuilder, RestEventApisBuilder
@Getter
public final class HttpServerRouter {

    private final Set<Class<? extends RestApi>> restApiClass = new HashSet<>();
    private final Set<Class<? extends RestEventApi>> restEventApiClass = new HashSet<>();
    private final Map<String, DynamicRestApi> dynamicRestApi = new HashMap<>();
    private final Set<WebsocketEventMetadata> websocketEvents = new HashSet<>();

    @SafeVarargs
    public final HttpServerRouter registerApi(Class<? extends RestApi>... apiClass) {
        restApiClass.addAll(Arrays.stream(apiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final HttpServerRouter registerEventBusApi(Class<? extends RestEventApi>... eventBusApiClass) {
        restEventApiClass.addAll(Arrays.stream(eventBusApiClass).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

    public HttpServerRouter registerEventBusSocket(WebsocketEventMetadata... eventBusSocket) {
        websocketEvents.addAll(Arrays.stream(eventBusSocket).filter(Objects::nonNull).collect(Collectors.toList()));
        return this;
    }

}
