package com.nubeiot.core.http.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.RestEventMetadata;

public abstract class AbstractRestEventApi implements RestEventApi {

    private final List<RestEventMetadata> restMetadata = new ArrayList<>();
    private final Map<EventAction, HttpMethod> httpEventMapping;

    protected AbstractRestEventApi() {
        this.httpEventMapping = Collections.unmodifiableMap(initHttpEventMap());
        initRoute();
    }

    protected abstract void initRoute();

    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return ActionMethodMapping.defaultEventHttpMap();
    }

    protected void addRouter(RestEventMetadata metadata) {
        restMetadata.add(metadata);
    }

    protected void addRouter(EventModel eventModel, String api) {
        addRouter(eventModel, api, null);
    }

    protected void addRouter(EventModel eventModel, String api, String paramName) {
        eventModel.getEvents().forEach(event -> {
            HttpMethod httpMethod = httpEventMapping.get(event);
            if (Objects.isNull(httpMethod)) {
                return;
            }
            restMetadata.add(RestEventMetadata.builder()
                                              .address(eventModel.getAddress())
                                              .pattern(eventModel.getPattern())
                                              .local(eventModel.isLocal())
                                              .action(event)
                                              .path(api)
                                              .method(httpMethod)
                                              .paramName(paramName)
                                              .build());
        });
    }

    @Override
    public List<RestEventMetadata> getRestMetadata() { return Collections.unmodifiableList(restMetadata); }

    @Override
    public Map<EventAction, HttpMethod> get() { return httpEventMapping; }

}
