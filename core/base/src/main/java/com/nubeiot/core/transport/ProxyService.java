package com.nubeiot.core.transport;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

public interface ProxyService {

    <T extends ProxyService> T init(@NonNull Vertx vertx, JsonObject config);

    Transporter transporter();

}
