package com.nubeiot.core.micro;

import java.util.Objects;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;

public interface ServiceGatewayMonitor extends Handler<Message<Object>> {

    static <T extends ServiceGatewayMonitor> T create(String className, @NonNull T fallback) {
        if (fallback.getClass().getName().equals(className) || Strings.isBlank(className)) {
            return fallback;
        }
        T monitor = ReflectionClass.createObject(className);
        return Objects.isNull(monitor) ? fallback : monitor;
    }

}
