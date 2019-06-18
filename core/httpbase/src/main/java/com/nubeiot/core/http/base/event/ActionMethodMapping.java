package com.nubeiot.core.http.base.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.event.EventAction;

import lombok.NonNull;

public interface ActionMethodMapping extends Supplier<Map<EventAction, HttpMethod>> {

    /**
     * Default mapping for common {@code CREATE | READ | UPDATE | DELETE} operations
     *
     * @see #defaultCRUDMap()
     */
    ActionMethodMapping CRUD_MAP = ActionMethodMapping.create(defaultCRUDMap());

    /**
     * Default mapping for common {@code CREATE | UPDATE | DELETE} operations
     *
     * @see #defaultCUDMap()
     */
    ActionMethodMapping CUD_MAP = ActionMethodMapping.create(defaultCUDMap());

    static ActionMethodMapping create(@NonNull Map<EventAction, HttpMethod> map) {
        return () -> Collections.unmodifiableMap(map);
    }

    static Map<EventAction, HttpMethod> defaultCRUDMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        map.put(EventAction.GET_ONE, HttpMethod.GET);
        map.put(EventAction.GET_LIST, HttpMethod.GET);
        return map;
    }

    static Map<EventAction, HttpMethod> defaultCUDMap() {
        Map<EventAction, HttpMethod> map = new HashMap<>();
        map.put(EventAction.CREATE, HttpMethod.POST);
        map.put(EventAction.UPDATE, HttpMethod.PUT);
        map.put(EventAction.PATCH, HttpMethod.PATCH);
        map.put(EventAction.REMOVE, HttpMethod.DELETE);
        return map;
    }

    default boolean hasDuplicateMethod() {
        return get().size() != new HashSet<>(get().values()).size();
    }

}
