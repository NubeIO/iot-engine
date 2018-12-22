package com.nubeiot.core.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;

import io.vertx.core.http.HttpMethod;

public abstract class AbstractRestEventApi implements RestEventApi {

    private final List<RestEventMetadata> restMetadata = new ArrayList<>();
    private final Map<EventAction, HttpMethod> httpEventMapping;

    protected AbstractRestEventApi() {
        this.httpEventMapping = Collections.unmodifiableMap(initHttpEventMap());
        initRoute();
    }

    protected abstract void initRoute();

    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return RestEventApi.defaultEventHttpMap();
    }

    protected void addRouter(EventModel eventModel, String api, String paramName) {
        eventModel.getEvents().forEach(event -> {
            final HttpMethod httpMethod = httpEventMapping.get(event);
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
    public List<RestEventMetadata> getRestMetadata() {
        return Collections.unmodifiableList(restMetadata);
    }

}
