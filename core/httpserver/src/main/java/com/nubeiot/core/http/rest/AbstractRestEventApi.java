package com.nubeiot.core.http.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;

public abstract class AbstractRestEventApi implements RestEventApi {

    private final List<RestEventApiMetadata> restMetadata = new ArrayList<>();
    private final Map<EventAction, HttpMethod> httpEventMapping;

    protected AbstractRestEventApi() {
        this.httpEventMapping = Collections.unmodifiableMap(initHttpEventMap());
        initRoute();
    }

    protected abstract void initRoute();

    protected Map<EventAction, HttpMethod> initHttpEventMap() {
        return ActionMethodMapping.defaultEventHttpMap();
    }

    protected void addRouter(RestEventApiMetadata metadata) {
        restMetadata.add(metadata);
    }

    protected void addRouter(EventModel eventModel, String apiPath) {
        addRouter(eventModel, apiPath, null);
    }

    protected void addRouter(EventModel eventModel, String apiPath, String capturePath) {
        restMetadata.add(RestEventApiMetadata.builder()
                                             .address(eventModel.getAddress())
                                             .pattern(eventModel.getPattern())
                                             .definition(EventMethodDefinition.create(apiPath, capturePath, this))
                                             .build());
    }

    @Override
    public List<RestEventApiMetadata> getRestMetadata() { return Collections.unmodifiableList(restMetadata); }

    @Override
    public Map<EventAction, HttpMethod> get() { return httpEventMapping; }

}
