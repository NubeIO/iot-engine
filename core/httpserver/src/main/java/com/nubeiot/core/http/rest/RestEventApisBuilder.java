package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.base.Urls;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.handler.RestEventResultHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

public final class RestEventApisBuilder {

    private final Logger logger = LoggerFactory.getLogger(RestEventApisBuilder.class);
    private final Router router;
    private final Set<Class<? extends RestEventApi>> apis = new HashSet<>();
    private final Map<Class<? extends RestEventApi>, RestEventResultHandler> restHandlers = new HashMap<>();

    /**
     * For test
     */
    RestEventApisBuilder() {
        this.router = null;
    }

    public RestEventApisBuilder(Vertx vertx) {
        this.router = Router.router(vertx);
    }

    public RestEventApisBuilder(io.vertx.reactivex.core.Vertx vertx) {
        this(vertx.getDelegate());
    }

    public RestEventApisBuilder(Router router) {
        this.router = router;
    }

    public RestEventApisBuilder(io.vertx.reactivex.ext.web.Router router) {
        this(router.getDelegate());
    }

    public RestEventApisBuilder register(@NonNull Class<? extends RestEventApi> restApi) {
        apis.add(restApi);
        return this;
    }

    @SafeVarargs
    public final RestEventApisBuilder register(Class<? extends RestEventApi>... restApi) {
        return this.register(Arrays.asList(restApi));
    }

    public RestEventApisBuilder register(@NonNull Collection<Class<? extends RestEventApi>> restApis) {
        restApis.stream().filter(Objects::nonNull).forEach(apis::add);
        return this;
    }

    public RestEventApisBuilder addHandler(@NonNull Class<? extends RestEventApi> restApi,
                                           RestEventResultHandler handler) {
        apis.add(restApi);
        restHandlers.put(restApi, handler);
        return this;
    }

    public Router build() {
        validate().stream().map(ReflectionClass::createObject).filter(Objects::nonNull).forEach(this::createRouter);
        return router;
    }

    Set<Class<? extends RestEventApi>> validate() {
        if (apis.isEmpty()) {
            throw new InitializerError("No REST API given, register at least one.");
        }
        return apis;
    }

    private void createRouter(RestEventApi restApi) {
        restApi.getRestMetadata()
               .stream()
               .sorted(Comparator.comparingInt(o -> o.getPath().length()))
               .forEach(metadata -> this.createRouter(metadata, restApi));
    }

    private void createRouter(RestEventApiMetadata metadata, RestEventApi api) {
        RestEventResultHandler restHandler = restHandlers.getOrDefault(api.getClass(),
                                                                       new RestEventResultHandler(metadata));
        String path = Urls.combinePath(metadata.getPath());
        logger.info("Registering route | Event Binding:\t{} {} --- {} {} {}", metadata.getMethod(), path,
                    metadata.getPattern(), metadata.getAction(), metadata.getAddress());
        router.route(metadata.getMethod(), path).produces(ApiConstants.DEFAULT_CONTENT_TYPE).handler(restHandler);
    }

}
