package com.nubeiot.core.http.rest;

import java.util.Arrays;
import java.util.Collection;
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
import com.nubeiot.core.http.InvalidUrlException;
import com.nubeiot.core.http.handler.RestEventResultHandler;
import com.nubeiot.core.http.utils.Urls;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

public final class RestEventBuilder {

    private final Logger logger = LoggerFactory.getLogger(RestEventBuilder.class);
    private final Router router;
    private final Set<Class<? extends RestEventApi>> apis = new HashSet<>();
    private final Map<Class<? extends RestEventApi>, RestEventResultHandler> restHandlers = new HashMap<>();
    @Getter
    private String rootApi = ApiConstants.ROOT_API_PATH;

    /**
     * For test
     */
    RestEventBuilder() {
        this.router = null;
    }

    public RestEventBuilder(Vertx vertx) {
        this.router = Router.router(vertx);
    }

    public RestEventBuilder(io.vertx.reactivex.core.Vertx vertx) {
        this(vertx.getDelegate());
    }

    public RestEventBuilder(Router router) {
        this.router = router;
    }

    public RestEventBuilder(io.vertx.reactivex.ext.web.Router router) {
        this(router.getDelegate());
    }

    public RestEventBuilder rootApi(String rootApi) {
        if (Strings.isNotBlank(rootApi)) {
            String root = Urls.combinePath(rootApi);
            if (!Urls.validatePath(root)) {
                throw new InvalidUrlException("Root API is not valid");
            }
            this.rootApi = root;
        }
        return this;
    }

    public RestEventBuilder register(@NonNull Class<? extends RestEventApi> restApi) {
        apis.add(restApi);
        return this;
    }

    @SafeVarargs
    public final RestEventBuilder register(Class<? extends RestEventApi>... restApi) {
        return this.register(Arrays.asList(restApi));
    }

    public RestEventBuilder register(@NonNull Collection<Class<? extends RestEventApi>> restApis) {
        restApis.stream().filter(Objects::nonNull).forEach(apis::add);
        return this;
    }

    public RestEventBuilder addHandler(@NonNull Class<? extends RestEventApi> restApi, RestEventResultHandler handler) {
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
        restApi.getRestMetadata().parallelStream().forEach(metadata -> this.createRouter(metadata, restApi));
    }

    private void createRouter(RestEventMetadata metadata, RestEventApi api) {
        RestEventResultHandler restHandler = restHandlers.getOrDefault(api.getClass(),
                                                                       new RestEventResultHandler(metadata));
        String path = Urls.combinePath(rootApi, metadata.getPath());
        logger.info("Registering route | Event Binding:\t{} {} --- {} {} {}", metadata.getMethod(), path,
                    metadata.getPattern(), metadata.getAction(), metadata.getAddress());
        router.route(metadata.getMethod(), path).produces(ApiConstants.DEFAULT_CONTENT_TYPE).handler(restHandler);
    }

}
