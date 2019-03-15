package com.nubeiot.core.http.rest;

import io.vertx.servicediscovery.types.EventBusService;

public interface DynamicRestEventApi extends DynamicRestApi {

    @Override
    default String type() {
        return EventBusService.TYPE;
    }

}
