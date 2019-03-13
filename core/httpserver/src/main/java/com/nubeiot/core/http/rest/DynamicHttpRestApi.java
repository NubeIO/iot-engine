package com.nubeiot.core.http.rest;

import io.vertx.servicediscovery.types.HttpEndpoint;

public interface DynamicHttpRestApi extends DynamicRestApi {

    @Override
    default String byType() {
        return HttpEndpoint.TYPE;
    }

}
