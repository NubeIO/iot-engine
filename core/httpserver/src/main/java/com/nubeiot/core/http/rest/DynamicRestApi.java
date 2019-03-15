package com.nubeiot.core.http.rest;

import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

import com.nubeiot.core.http.ApiConstants;

import lombok.NonNull;

/**
 * Dynamic REST API that backed by {@code Service Discovery}
 * <p>
 * To use it, your project must depends on {@code :core:micro}
 */
public interface DynamicRestApi {

    /**
     * HTTP path for Gateway server
     *
     * @return HTTP path for gateway server
     */
    @NonNull String path();

    /**
     * Service type
     *
     * @return Service Type
     * @see Record#getType()
     * @see HttpEndpoint#TYPE
     */
    @NonNull String type();

    /**
     * Service name
     *
     * @return Service Name
     * @see Record#getName()
     */
    @NonNull String name();

    /**
     * Metadata to help reaching out service
     *
     * @return service metadata, it might be {@code null}
     */
    JsonObject byMetadata();

    /**
     * Supported {@code HTTP methods} by service. {@code Default: } all http methods
     *
     * @return set of {@code HTTP methods}
     */
    default Set<HttpMethod> availableMethods() {
        return ApiConstants.DEFAULT_CORS_HTTP_METHOD;
    }

}
