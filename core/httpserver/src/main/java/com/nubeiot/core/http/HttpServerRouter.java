package com.nubeiot.core.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public final class HttpServerRouter {

    private final Set<Class> restApiClass = new HashSet<>();
    private final Set<Class<? extends EventBusRestApi>> eventBusRestApiClass = new HashSet<>();

    public HttpServerRouter registerApi(Class... apiClass) {
        restApiClass.addAll(Arrays.asList(apiClass));
        return this;
    }

    @SuppressWarnings("unchecked")
    public HttpServerRouter registerEventBusApi(Class<? extends EventBusRestApi>... eventBusApiClass) {
        eventBusRestApiClass.addAll(Arrays.asList(eventBusApiClass));
        return this;
    }

    boolean validate() {
        return hasApi() || hasEventBusApi();
    }

    boolean hasApi() {
        return !restApiClass.isEmpty();
    }

    boolean hasEventBusApi() {
        return !eventBusRestApiClass.isEmpty();
    }

}
