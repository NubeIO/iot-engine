package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventType;
import com.nubeiot.core.http.utils.Urls;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.ext.web.Router;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class EventBusRestApi implements IEventBusRestApi {

    @Getter
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<EventType, HttpMethod> httpEventMapping;
    @Getter(value = AccessLevel.PROTECTED)
    private final List<Metadata> routers = new ArrayList<>();

    protected EventBusRestApi() {
        this.httpEventMapping = Collections.unmodifiableMap(initHttpEventMapping());
    }

    protected abstract Map<EventType, HttpMethod> initHttpEventMapping();

    protected void addRouter(EventModel eventModel, String api, String paramName) {
        eventModel.getEvents().parallelStream().forEach(event -> {
            final HttpMethod httpMethod = httpEventMapping.get(event);
            if (Objects.isNull(httpMethod)) {
                return;
            }
            routers.add(Metadata.builder()
                                .address(eventModel.getAddress())
                                .action(event)
                                .path(api)
                                .method(httpMethod)
                                .paramName(paramName)
                                .build());
        });
    }

    @Override
    public void register(EventBus eventBus, Router router) {
        routers.parallelStream().forEach(metadata -> createRouter(eventBus, router, metadata));
    }

    private void createRouter(EventBus eventBus, Router router, Metadata metadata) {
        final String path = Urls.combinePath(ApiConstants.ROOT_API_PATH, metadata.getPath());
        logger.info("Registering route | Event Binding:\t{} {} --- {} {}", metadata.getMethod(), path,
                    metadata.getAction(), metadata.getAddress());
        router.route(metadata.getMethod(), path)
              .produces(ApiConstants.DEFAULT_CONTENT_TYPE)
              .handler(ctx -> registerAppControllerHandler(eventBus, ctx, metadata));
    }

}
