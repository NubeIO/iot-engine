package com.nubeiot.core.http.client.handler;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Supplier;

import io.reactivex.SingleEmitter;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NubeExceptionConverter;
import com.nubeiot.core.exceptions.TimeoutException;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class HttpClientErrorHandler implements Handler<Throwable>, Supplier<SingleEmitter<ResponseData>> {

    @NonNull
    private final SingleEmitter<ResponseData> emitter;

    @SuppressWarnings("unchecked")
    public static <T extends HttpClientErrorHandler> T create(SingleEmitter<ResponseData> emitter,
                                                              Class<T> endHandlerClass) {
        if (Objects.isNull(endHandlerClass) || HttpClientErrorHandler.class.equals(endHandlerClass)) {
            return (T) new HttpClientErrorHandler(emitter) {};
        }
        return (T) ReflectionClass.createObject(endHandlerClass, new LinkedHashMap<>(
            Collections.singletonMap(SingleEmitter.class, emitter))).get();
    }

    @Override
    public void handle(Throwable error) {
        if (error instanceof VertxException && error.getMessage().equals("Connection was closed")) {
            emitter.onError(new TimeoutException("Request timeout", error));
            return;
        }
        emitter.onError(NubeExceptionConverter.friendly(error));
    }

    @Override
    public SingleEmitter<ResponseData> get() {
        return emitter;
    }

}
