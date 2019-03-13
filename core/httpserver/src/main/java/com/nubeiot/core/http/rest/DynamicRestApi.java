package com.nubeiot.core.http.rest;

import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import lombok.NonNull;

/**
 * Service Discovery
 */
public interface DynamicRestApi {

    /**
     * Http endpoint path to redirect
     *
     * @return HTTP endpoint path
     */
    @NonNull String byPath();

    /**
     * To getFilter in ServiceDiscovery
     *
     * @return ServiceDiscovery Type
     */
    @NonNull String byType();

    /**
     * To getFilter in ServiceDiscovery
     *
     * @return ServiceDiscovery Name
     */
    @NonNull String byName();

    /**
     * To getFilter in ServiceDiscovery
     *
     * @return ServiceDiscovery Metadata, it might {@code null}
     */
    JsonObject byMetadata();

    Set<HttpMethod> availableMethods();

}
