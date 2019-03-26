package com.nubeiot.core.http.client.handler;

import java.util.Objects;
import java.util.function.BiFunction;

import io.vertx.core.http.HttpClientRequest;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.utils.Reflections.ReflectionClass;

import lombok.NonNull;

/**
 * Represents for {@code HTTP client} write data (header/body/cookie) into request before actual sending to specific
 * server.
 * <b>Notice:</b> Never close {@code HTTPClientRequest} in this function
 */
public interface HttpClientWriter extends BiFunction<HttpClientRequest, RequestData, HttpClientRequest> {

    HttpClientWriter DEFAULT = new HttpClientWriter() {};

    @SuppressWarnings("unchecked")
    static <T extends HttpClientWriter> T create(Class<T> writerClass) {
        return Objects.isNull(writerClass) || HttpClientWriter.class.equals(writerClass)
               ? (T) DEFAULT
               : ReflectionClass.createObject(writerClass);
    }

    @Override
    default HttpClientRequest apply(@NonNull HttpClientRequest request, RequestData reqData) {
        if (Objects.isNull(reqData)) {
            return request;
        }
        if (!reqData.headers().isEmpty()) {
            request.headers().setAll(HttpHeaderUtils.deserializeHeaders(reqData.headers()));
        }
        if (Objects.nonNull(reqData.body()) && !reqData.body().isEmpty()) {
            request.write(reqData.body().toBuffer());
        }
        return request;
    }

}
