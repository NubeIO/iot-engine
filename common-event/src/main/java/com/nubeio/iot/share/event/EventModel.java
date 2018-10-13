package com.nubeio.iot.share.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.http.HttpMethod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class EventModel {

    //TODO
    public static final EventModel MODULE_INSTALLER = new EventModel("com.nubeio.io.edge.app-installer").put(
            EventType.CREATE, "/api/module", HttpMethod.POST)
                                                                                                        .put(EventType.UPDATE,
                                                                                                             "/api" +
                                                                                                             "/module",
                                                                                                             HttpMethod.PUT)
                                                                                                        .put(EventType.REMOVE,
                                                                                                             "/api" +
                                                                                                             "/module",
                                                                                                             HttpMethod.DELETE);

    @Getter
    @EqualsAndHashCode.Include
    private final String address;
    private final Map<EventType, Metadata> eventMap = new HashMap<>();

    public EventModel put(EventType eventType, String eventEndpoint, HttpMethod method) {
        this.eventMap.put(eventType, new Metadata(eventType, eventEndpoint, method));
        return this;
    }

    public Metadata get(EventType eventType) {
        return this.eventMap.get(eventType);
    }

    public Map<EventType, Metadata> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Metadata {

        private final EventType action;
        private final String endpoint;
        private final HttpMethod method;

    }

}
