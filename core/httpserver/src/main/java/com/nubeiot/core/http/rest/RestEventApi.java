package com.nubeiot.core.http.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;

/**
 * Make a mapping dynamically between {@code HTTP endpoint} and {@code EventBus}
 */
public interface RestEventApi {

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

    List<RestEventMetadata> getRestMetadata();

}
