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
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpStatusMapping;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;
import com.nubeiot.core.http.base.HttpUtils.HttpHeaderUtils;
import com.nubeiot.core.utils.Reflections.ReflectionClass;
import com.nubeiot.core.utils.Strings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class LightweightResponseBodyHandler implements Handler<Buffer> {

    private final static String SUCCESS_KEY = "data";
    private final static String ERROR_KEY = "error";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @NonNull
    private final HttpClientResponse response;
    @NonNull
    private final SingleEmitter<ResponseData> emitter;
    private final boolean shadowError;

    @SuppressWarnings("unchecked")
    public static <T extends LightweightResponseBodyHandler> T create(HttpClientResponse response,
                                                                      SingleEmitter<ResponseData> emitter,
                                                                      boolean swallowError, Class<T> bodyHandlerClass) {
        Map<Class, Object> params = new LinkedHashMap<>();
        params.put(HttpClientResponse.class, response);
        params.put(SingleEmitter.class, emitter);
        params.put(boolean.class, swallowError);
        return Objects.isNull(bodyHandlerClass) || LightweightResponseBodyHandler.class.equals(bodyHandlerClass)
               ? (T) new LightweightResponseBodyHandler(response, emitter, swallowError) {}
               : ReflectionClass.createObject(bodyHandlerClass, params);
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
        if (Strings.isNotBlank(contentType) && contentType.contains("json")) {
            logger.info("Try parsing Json data from {}::{}", method, uri);
            return tryParse(buffer, true);
        }
        logger.warn("Try parsing Json in ambiguous case from {}::{}", method, uri);
        return tryParse(buffer, false);
    }

    private JsonObject tryParse(Buffer buffer, boolean isJson) {
        try {
            return buffer.toJsonObject();
        } catch (DecodeException e) {
            logger.trace("Failed to parse json. Try json array", e);
            String key = response.statusCode() >= 400 ? ERROR_KEY : SUCCESS_KEY;
            JsonObject data = new JsonObject();
            try {
                data.put(key, buffer.toJsonArray());
            } catch (DecodeException ex) {
                if (isJson) {
                    throw new NubeException(ErrorCode.HTTP_ERROR,
                                            "Http Server doesn't return json data. Received data: " + buffer.toString(),
                                            ex);
                }
                logger.trace("Failed to parse json array. Use text", ex);
            }
            //TODO check length, check encode
            data.put(key, buffer.toString());
            return data;
        }
    }

}
