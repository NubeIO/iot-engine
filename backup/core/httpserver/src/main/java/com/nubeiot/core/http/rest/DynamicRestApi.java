package com.nubeiot.core.http.rest;

import java.util.Optional;
import java.util.Set;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.HttpUtils;
import com.nubeiot.core.micro.type.EventMessageService;

import lombok.NonNull;

/**
 * Dynamic REST API that backed by {@code Service Discovery}
 * <p>
 * To use it, your project must depends on {@code :core:micro}
 */
public interface DynamicRestApi {

    @SuppressWarnings("unchecked")
    static <T extends DynamicRestApi> T create(@NonNull Record record) {
        if (HttpEndpoint.TYPE.equals(record.getType())) {
            return (T) DynamicHttpRestApi.create(record);
        }
        if (EventMessageService.TYPE.equals(record.getType())) {
            return (T) DynamicEventRestApi.create(record);
        }
        throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Dynamic Rest API unsupported type " + record.getType());
    }

    /**
     * Service root path
     *
     * @return HTTP path for dynamic service
     */
    String path();

    /**
     * Router order
     *
     * @return Router order
     */
    int order();

    /**
     * Identify using whether {@code RequestData} or not
     *
     * @return {@code true} if using {@code RequestData}
     */
    boolean useRequestData();

    /**
     * It is capturePath paths. It might be empty
     *
     * @return alternative paths
     */
    Optional<Set<String>> alternativePaths();

    /**
     * Service type
     *
     * @return Service Type
     */
    @NonNull String type();

    /**
     * Service name
     *
     * @return Service Name
     */
    @NonNull String name();

    /**
     * Metadata to help reaching out service
     *
     * @return service metadata, it might be {@code null}
     */
    JsonObject byMetadata();

    /**
     * Supported {@code HTTP methods} by service. {@code Default: all http methods}
     *
     * @return set of {@code HTTP methods}
     */
    default Set<HttpMethod> availableMethods() {
        return HttpUtils.DEFAULT_CORS_HTTP_METHOD;
    }

}
