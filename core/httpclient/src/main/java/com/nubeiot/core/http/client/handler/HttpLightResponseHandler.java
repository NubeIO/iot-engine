package com.nubeiot.core.http.client.handler;

import io.reactivex.SingleEmitter;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;

import com.nubeiot.core.dto.ResponseData;

import lombok.RequiredArgsConstructor;

/**
 * Represents for handling lightweight {@code HTTP response data}
 */
@RequiredArgsConstructor
public final class HttpLightResponseHandler<T extends HttpLightResponseBodyHandler>
    implements Handler<HttpClientResponse> {

    private final Class<T> bodyHandlerClass;
    private final SingleEmitter<ResponseData> emitter;
    private final boolean swallowError;

    @Override
    public void handle(HttpClientResponse response) {
        T bodyHandler = HttpLightResponseBodyHandler.create(response, emitter, swallowError, bodyHandlerClass);
        response.bodyHandler(bodyHandler).exceptionHandler(emitter::onError);
    }

}
