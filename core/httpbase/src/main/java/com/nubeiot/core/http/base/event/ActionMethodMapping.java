package com.nubeiot.core.http.base.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;

public interface ActionMethodMapping extends Supplier<Map<EventAction, HttpMethod>> {

    static Map<EventAction, HttpMethod> defaultEventHttpMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        return map;
    }

    @Override
    default Map<EventAction, HttpMethod> get() {
        return Collections.unmodifiableMap(defaultEventHttpMap());
    }

}
