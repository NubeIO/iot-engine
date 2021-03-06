package com.nubeiot.core.http.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.core.http.base.event.ActionMethodMapping;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.base.event.RestEventApiMetadata;

import lombok.NonNull;

public abstract class AbstractRestEventApi extends AbstractSharedDataDelegate<RestEventApi> implements RestEventApi {

    private final SortedMap<String, RestEventApiMetadata> restMetadata = new TreeMap<>(
        Comparator.comparingInt(String::length));
    private final ActionMethodMapping mapping;

    protected AbstractRestEventApi() {
        this.mapping = initHttpEventMap();
    }

    protected ActionMethodMapping initHttpEventMap() {
        return ActionMethodMapping.CRUD_MAP;
    }

    protected void addRouter(@NonNull EventModel eventModel, String apiPath) {
        addRouter(eventModel, EventMethodDefinition.create(apiPath, this));
    }

    protected void addRouter(@NonNull EventModel eventModel, String apiPath, String paramPath) {
        addRouter(eventModel, EventMethodDefinition.create(apiPath, paramPath, this));
    }

    protected void addRouter(@NonNull EventModel eventModel, @NonNull EventMethodDefinition definition) {
        addRouter(eventModel.getAddress(), eventModel.getPattern(), definition);
    }

    protected void addRouter(@NonNull String address, @NonNull EventPattern pattern,
                             @NonNull EventMethodDefinition definition) {
        if (restMetadata.containsKey(definition.getServicePath())) {
            logger.warn("HTTP path '{}' is already registered, but might different Event address '{}'",
                        definition.getServicePath(), restMetadata.get(definition.getServicePath()).getAddress());
        }
        restMetadata.putIfAbsent(definition.getServicePath(), RestEventApiMetadata.builder()
                                                                                  .address(address)
                                                                                  .pattern(pattern)
                                                                                  .definition(definition)
                                                                                  .build());
    }

    @Override
    public Collection<RestEventApiMetadata> getRestMetadata() {
        return Collections.unmodifiableCollection(restMetadata.values());
    }

    @Override
    public Map<EventAction, HttpMethod> get() { return mapping.get(); }

}
