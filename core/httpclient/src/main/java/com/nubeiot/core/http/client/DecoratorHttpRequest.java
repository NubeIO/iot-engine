package com.nubeiot.core.http.client;

import java.util.Objects;
import java.util.function.BiFunction;

import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HttpUtils.HttpRequests;

/**
 * Add decorator for HTTP Request
 *
 * <b>Notice:</b> Never close {@code HTTPClientRequest} in this function
 */
public class DecoratorHttpRequest implements BiFunction<HttpClientRequest, RequestData, HttpClientRequest> {

    @Override
    public HttpClientRequest apply(HttpClientRequest request, RequestData requestData) {
        if (Objects.isNull(requestData)) {
            return request;
        }
        if (!requestData.headers().isEmpty()) {
            request.headers()
                   .setAll(HttpRequests.deserializeHeaders(requestData.headers()))
                   .remove(HttpHeaders.ACCEPT_ENCODING)
                   .remove(HttpHeaders.CONTENT_TYPE)
                   .remove(HttpHeaders.CONTENT_LENGTH);
        }
        if (!requestData.body().isEmpty()) {
            request.write(requestData.body().toBuffer());
        }
        return request;
    }

}
