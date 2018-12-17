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

public abstract class AbstractEventBusRestApi implements EventBusRestApi {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<EventBusRestMetadata> restMetadata = new ArrayList<>();
    private final Map<EventType, HttpMethod> httpEventMapping;

    protected AbstractEventBusRestApi() {
        this.httpEventMapping = Collections.unmodifiableMap(initHttpEventMap());
        initRoute();
    }

    protected abstract void initRoute();

    protected Map<EventType, HttpMethod> initHttpEventMap() {
        return EventBusRestApi.defaultEventHttpMap();
    }

    protected void addRouter(EventModel eventModel, String api, String paramName) {
        eventModel.getEvents().forEach(event -> {
            final HttpMethod httpMethod = httpEventMapping.get(event);
            if (Objects.isNull(httpMethod)) {
                return;
            }
            restMetadata.add(EventBusRestMetadata.builder()
                                                 .address(eventModel.getAddress())
                                                 .action(event)
                                                 .path(api)
                                                 .method(httpMethod)
                                                 .paramName(paramName)
                                                 .build());
        });
    }

    @Override
    public List<EventBusRestMetadata> getRestMetadata() {
        return Collections.unmodifiableList(restMetadata);
    }

    @Override
    public void register(EventBus eventBus, Router router) {
        this.getRestMetadata().parallelStream().forEach(metadata -> createRouter(eventBus, router, metadata));
    }

    private void createRouter(EventBus eventBus, Router router, EventBusRestMetadata metadata) {
        String path = Urls.combinePath(ApiConstants.ROOT_API_PATH, metadata.getPath());
        logger.info("Registering route | Event Binding:\t{} {} --- {} {}", metadata.getMethod(), path,
                    metadata.getAction(), metadata.getAddress());
        router.route(metadata.getMethod(), path)
              .produces(ApiConstants.DEFAULT_CONTENT_TYPE)
              .handler(ctx -> registerAppControllerHandler(eventBus, ctx, metadata));
    }

}
