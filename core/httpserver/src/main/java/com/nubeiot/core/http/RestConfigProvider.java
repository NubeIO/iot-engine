package com.nubeiot.core.http;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestConfigProvider {

    @Getter
    private final JsonObject config;

    @Getter
    private final JsonObject appConfig;

}
