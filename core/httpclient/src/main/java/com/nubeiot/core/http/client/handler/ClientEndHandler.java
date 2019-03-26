package com.nubeiot.core.http.client.handler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Supplier;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;

import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ClientEndHandler implements Handler<Void>, Supplier<HttpClient> {

    @NonNull
    private final HttpClient client;

    @SuppressWarnings("unchecked")
    public static <T extends ClientEndHandler> T create(HttpClient client, Class<T> endHandlerClass) {
        if (Objects.isNull(endHandlerClass) || ClientEndHandler.class.equals(endHandlerClass)) {
            return (T) new ClientEndHandler(client) {};
        }
        return (T) ReflectionClass.createObject(endHandlerClass,
                                                new LinkedHashMap<>(Collections.singletonMap(HttpClient.class, client)))
                                  .get();
    }

    @Override
    public void handle(Void event) { get().close(); }

    @Override
    public HttpClient get() { return client; }

}
