package com.nubeiot.core.http.rest;

import io.vertx.servicediscovery.types.EventBusService;

public interface DynamicRestEventApi extends DynamicRestApi {

    @Override
    default String byType() {
        return EventBusService.TYPE;
    }

}
