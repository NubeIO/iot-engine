package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public final class HttpServerRouter {

    private final Set<Class> restApiClass = new HashSet<>();
    private final Set<Class<? extends IEventBusRestApi>> eventBusRestApiClass = new HashSet<>();

    public HttpServerRouter registerApi(Class... apiClass) {
        restApiClass.addAll(Arrays.asList(apiClass));
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpServerRouter registerEventBusApi(Class<? extends IEventBusRestApi>... eventBusApiClass) {
        eventBusRestApiClass.addAll(Arrays.asList(eventBusApiClass));
        return this;
    }

}
