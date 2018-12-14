package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.nubeiot.core.utils.Reflections;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;

public final class EventBusRestBuilder {

    private final Vertx vertx;
    private final Router router;
    private final EventBus eventBus;
    private final List<Class<? extends IEventBusRestApi>> apis = new ArrayList<>();

    public EventBusRestBuilder(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(this.vertx);
        this.eventBus = this.vertx.eventBus();
    }

    public EventBusRestBuilder(io.vertx.core.Vertx vertx) {
        this(Vertx.newInstance(vertx));
    }

    public EventBusRestBuilder(Router router, EventBus eventBus) {
        this.vertx = null;
        this.router = router;
        this.eventBus = eventBus;
    }

    public EventBusRestBuilder(io.vertx.ext.web.Router router, io.vertx.core.eventbus.EventBus eventBus) {
        this(Router.newInstance(router), EventBus.newInstance(eventBus));
    }

    @SuppressWarnings("unchecked")
    public EventBusRestBuilder register(Class<? extends IEventBusRestApi>... restApi) {
        Objects.requireNonNull(restApi, "Missing REST API");
        apis.addAll(Arrays.asList(restApi));
        return this;
    }

    public Router build() {
        if (apis.isEmpty()) {
            throw new IllegalArgumentException("No REST API given, register at least one.");
        }
        apis.stream()
            .map(Reflections::createObject)
            .filter(Objects::nonNull)
            .forEach(api -> api.register(this.eventBus, this.router));
        return router;
    }

}
