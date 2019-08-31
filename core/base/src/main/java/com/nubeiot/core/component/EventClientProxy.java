package com.nubeiot.core.component;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.transport.ProxyService;

import lombok.NonNull;

public interface EventClientProxy extends ProxyService {

    static EventClientProxy create(@NonNull Vertx vertx, JsonObject config) {
        return () -> new DefaultEventClient(vertx, config);
    }

}
