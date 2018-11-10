package com.nubeio.iot.share.event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.nubeio.iot.share.dto.RequestData;
import com.nubeio.iot.share.exceptions.NubeException;
import com.nubeio.iot.share.exceptions.StateException;
import com.nubeio.iot.share.utils.Reflections;

import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;

public abstract class EventHandler implements IEventHandler {

    static Method getMethodByAnnotation(@NonNull Class<?> clazz, @NonNull EventType event) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isPrivate(method.getModifiers())) {
                continue;
            }
            EventContractor declaredOne = method.getAnnotation(EventContractor.class);
            if (Stream.of(declaredOne.values()).anyMatch(eventType -> event == eventType)) {
                return method;
            }
        }
        return null;
    }

    protected abstract List<EventType> getAvailableEvents();

    @SuppressWarnings("unchecked")
    @Override
    public final Single<JsonObject> handle(EventType eventType, RequestData data) throws NubeException {
        if (!this.getAvailableEvents().contains(eventType)) {
            throw new StateException("Unsupported event " + eventType);
        }
        final Method method = getMethodByAnnotation(this.getClass(), eventType);
        if (Objects.isNull(method)) {
            return null;
        }
        return (Single<JsonObject>) Reflections.executeMethod(this, method, data, Single.class);
    }

}
