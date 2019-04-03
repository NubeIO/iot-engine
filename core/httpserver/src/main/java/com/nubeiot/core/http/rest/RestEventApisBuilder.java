package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.ApiConstants;
import com.nubeiot.core.http.base.event.EventMethodDefinition.EventMethodMapping;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.handler.RestEventResultHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class RestEventApisBuilder {

    private final Logger logger = LoggerFactory.getLogger(RestEventApisBuilder.class);
    private final Router router;
    private final Set<Class<? extends RestEventApi>> apis = new HashSet<>();
    private EventController eventController;

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

    public RestEventApisBuilder addEventController(EventController eventController) {
        this.eventController = eventController;
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
               .stream().sorted(Comparator.comparingInt(o -> o.getDefinition().getServicePath().length()))
               .forEach(metadata -> this.createRouter(metadata, restApi));
    }

    private void createRouter(RestEventApiMetadata metadata, RestEventApi api) {
        for (EventMethodMapping mapping : metadata.getDefinition().getMapping()) {
            RestEventResultHandler restHandler = RestEventResultHandler.create(api.handler(), eventController,
                                                                               metadata.getAddress(),
                                                                               mapping.getAction(),
                                                                               metadata.getPattern());
            final String path = Strings.isBlank(mapping.getCapturePath())
                                ? metadata.getDefinition().getServicePath()
                                : mapping.getCapturePath();
            logger.info("Registering route | Event Binding:\t{} {} --- {} {} {}", mapping.getMethod(), path,
                        metadata.getPattern(), mapping.getAction(), metadata.getAddress());
            router.route(mapping.getMethod(), path).produces(ApiConstants.DEFAULT_CONTENT_TYPE).handler(restHandler);
        }
    }

}
