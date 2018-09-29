package io.nubespark.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.http.HttpMethod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class Event {

    public static final Event CONTROL_MODULE = new Event("io.nubespark.app.installer")
                                                       .put("install", "/api/module", HttpMethod.POST)
                                                       .put("update", "/api/module", HttpMethod.PUT)
                                                       .put("uninstall", "/api/module", HttpMethod.DELETE);
    @Getter
    @EqualsAndHashCode.Include
    private final String address;
    private final Map<String, Metadata> eventMap = new HashMap<>();

    public Event put(String eventName, String eventEndpoint, HttpMethod method) {
        this.eventMap.put(eventName, new Metadata(eventName, eventEndpoint, method));
        return this;
    }

    public Metadata get(String eventName) {
        return this.eventMap.get(eventName);
    }

    public Map<String, Metadata> getEventMap() {
        return Collections.unmodifiableMap(eventMap);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Metadata {

        private final String action;
        private final String endpoint;
        private final HttpMethod method;

    }

}
