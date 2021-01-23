package com.nubeiot.core.http.client.handler;

import io.reactivex.SingleEmitter;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;

import com.nubeiot.core.dto.ResponseData;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for handling lightweight {@code HTTP response data}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpLightResponseHandler<T extends HttpLightResponseBodyHandler>
    implements Handler<HttpClientResponse> {

    @NonNull
    private final SingleEmitter<ResponseData> emitter;
    @NonNull
    private final Class<T> bodyHandlerClass;
    private final boolean swallowError;

    public static <H extends HttpLightResponseBodyHandler> Handler<HttpClientResponse> create(
        @NonNull SingleEmitter<ResponseData> emitter, boolean swallowError, @NonNull Class<H> bodyHandlerClass) {
        return new HttpLightResponseHandler<>(emitter, bodyHandlerClass, swallowError);
    }

    @Override
    public void handle(HttpClientResponse response) {
        T bodyHandler = HttpLightResponseBodyHandler.create(response, emitter, swallowError, bodyHandlerClass);
        response.bodyHandler(bodyHandler).exceptionHandler(emitter::onError);
    }

}
