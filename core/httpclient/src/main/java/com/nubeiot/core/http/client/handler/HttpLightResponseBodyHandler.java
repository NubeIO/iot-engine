package com.nubeiot.core.http.client.handler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.reactivex.SingleEmitter;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for handler {@code HTTP Response}
 */
@RequiredArgsConstructor
public abstract class HttpLightResponseBodyHandler implements Handler<Buffer> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final HttpClientResponse response;
    @NonNull
    private final SingleEmitter<ResponseData> emitter;
    private final boolean shadowError;

    @SuppressWarnings("unchecked")
    public static <T extends HttpLightResponseBodyHandler> T create(HttpClientResponse response,
                                                                    SingleEmitter<ResponseData> emitter,
                                                                    boolean swallowError, Class<T> bodyHandlerClass) {
        if (Objects.isNull(bodyHandlerClass) || HttpLightResponseBodyHandler.class.equals(bodyHandlerClass)) {
            return (T) new HttpLightResponseBodyHandler(response, emitter, swallowError) {};
        }
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(HttpClientResponse.class, response);
        params.put(SingleEmitter.class, emitter);
        params.put(boolean.class, swallowError);
        return ReflectionClass.createObject(bodyHandlerClass, params);
    }

    @Override
    public void handle(Buffer buffer) {
        final JsonObject body = tryParse(buffer);
        if (!shadowError && response.statusCode() >= 400) {
            ErrorCode code = HttpStatusMapping.error(response.request().method(), response.statusCode());
            emitter.onError(new NubeException(code, body.encode()));
            return;
        }
        emitter.onSuccess(new ResponseData().setStatus(response.statusCode())
                                            .setHeaders(HttpHeaderUtils.serializeHeaders(response.headers()))
                                            .setBody(body));
    }

    private JsonObject tryParse(Buffer buffer) {
        String contentType = response.getHeader(HttpHeaders.CONTENT_TYPE);
        final HttpMethod method = response.request().method();
        final String uri = response.request().absoluteURI();
        final boolean isError = response.statusCode() >= 400;
        if (Strings.isNotBlank(contentType) && contentType.contains("json")) {
            logger.info("Try parsing Json data from {}::{}", method, uri);
            return JsonData.tryParse(buffer, true, isError).toJson();
        }
        logger.warn("Try parsing Json in ambiguous case from {}::{}", method, uri);
        return JsonData.tryParse(buffer, false, isError).toJson();
    }

}
