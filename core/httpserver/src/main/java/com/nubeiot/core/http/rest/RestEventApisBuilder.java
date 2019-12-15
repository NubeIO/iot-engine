package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.exceptions.InitializerError;
import com.nubeiot.core.http.HttpServer;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.base.event.EventMethodMapping;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;
import com.nubeiot.core.http.handler.RestEventApiDispatcher;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public final class RestEventApisBuilder {

    private final Logger logger = LoggerFactory.getLogger(RestEventApisBuilder.class);
    private final Router router;
    private final Set<Class<? extends RestEventApi>> apis = new HashSet<>();
    private Function<String, Object> sharedDataFunc;

    /**
     * For test
     */
    RestEventApisBuilder() {
        this.router = null;
    }

    public RestEventApisBuilder(Vertx vertx) {
        this.router = Router.router(vertx);
    }

    public RestEventApisBuilder addSharedDataFunc(@NonNull Function<String, Object> func) {
        this.sharedDataFunc = func;
        return this;
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
        restApi.registerSharedData(sharedDataFunc)
               .initRouter()
               .getRestMetadata()
               .forEach(metadata -> this.createRouter(metadata, restApi));
    }

    private void createRouter(RestEventApiMetadata metadata, RestEventApi api) {
        final EventMethodDefinition definition = metadata.getDefinition();
        EventbusClient controller = (EventbusClient) sharedDataFunc.apply(SharedDataDelegate.SHARED_EVENTBUS);
        for (EventMethodMapping mapping : definition.getMapping()) {
            RestEventApiDispatcher restHandler = RestEventApiDispatcher.create(api.dispatcher(), controller,
                                                                               metadata.getAddress(),
                                                                               mapping.getAction(),
                                                                               metadata.getPattern(),
                                                                               definition.isUseRequestData());
            final String path = Strings.isBlank(mapping.getCapturePath())
                                ? definition.getServicePath()
                                : mapping.getCapturePath();
            logger.info("Registering route | Event Binding:\t{} {} --- {} {} {}", mapping.getMethod(), path,
                        metadata.getPattern(), mapping.getAction(), metadata.getAddress());
            HttpServer.restrictJsonRoute(
                router.route(mapping.getMethod(), path).order(definition.getOrder()).handler(restHandler));
        }
    }

}
