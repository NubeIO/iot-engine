package com.nubeiot.core.http.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventContractor;
import com.nubeiot.core.event.EventHandler;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Upload listener to handle uploaded file (update database, transfer to another host)
 */
@RequiredArgsConstructor
public class UploadListener implements EventHandler {

    private final List<EventAction> actions;

    public static UploadListener create(String listenerClass, @NonNull List<EventAction> actions) {
        if (Strings.isBlank(listenerClass) || UploadListener.class.getName().equals(listenerClass)) {
            return new UploadListener(actions);
        }
        Map<Class, Object> inputs = new LinkedHashMap<>();
        inputs.put(List.class, actions);
        return ReflectionClass.createObject(listenerClass, inputs);
    }

    @Override
    public @NonNull List<EventAction> getAvailableEvents() { return actions; }

    @EventContractor(action = EventAction.CREATE)
    public JsonObject create(JsonObject data) { return data; }

}
