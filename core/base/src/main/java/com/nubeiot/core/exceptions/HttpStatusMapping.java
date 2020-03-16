package com.nubeiot.core.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;

import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @see ErrorCode
 */
//    TODO need more update
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpStatusMapping {

    private static final Map<ErrorCode, HttpResponseStatus> STATUS_ERROR = init();
    private static final Map<ErrorCode, Map<HttpMethod, HttpResponseStatus>> STATUS_METHOD_ERROR = initMethod();

    private static Map<ErrorCode, Map<HttpMethod, HttpResponseStatus>> initMethod() {
        Map<ErrorCode, Map<HttpMethod, HttpResponseStatus>> map = new EnumMap<>(ErrorCode.class);

        Map<HttpMethod, HttpResponseStatus> notFound = new EnumMap<>(HttpMethod.class);
        Arrays.stream(HttpMethod.values()).forEach(method -> notFound.put(method, HttpResponseStatus.GONE));
        notFound.put(HttpMethod.GET, HttpResponseStatus.NOT_FOUND);
        map.put(ErrorCode.NOT_FOUND, notFound);

        return Collections.unmodifiableMap(map);
    }

    private static Map<ErrorCode, HttpResponseStatus> init() {
        Map<ErrorCode, HttpResponseStatus> map = new EnumMap<>(ErrorCode.class);
        map.put(ErrorCode.INVALID_ARGUMENT, HttpResponseStatus.BAD_REQUEST);
        map.put(ErrorCode.HTTP_ERROR, HttpResponseStatus.BAD_REQUEST);

        map.put(ErrorCode.ALREADY_EXIST, HttpResponseStatus.UNPROCESSABLE_ENTITY);
        map.put(ErrorCode.BEING_USED, HttpResponseStatus.UNPROCESSABLE_ENTITY);

        map.put(ErrorCode.STATE_ERROR, HttpResponseStatus.CONFLICT);

        map.put(ErrorCode.AUTHENTICATION_ERROR, HttpResponseStatus.UNAUTHORIZED);
        map.put(ErrorCode.SECURITY_ERROR, HttpResponseStatus.FORBIDDEN);
        map.put(ErrorCode.INSUFFICIENT_PERMISSION_ERROR, HttpResponseStatus.FORBIDDEN);

        map.put(ErrorCode.EVENT_ERROR, HttpResponseStatus.SERVICE_UNAVAILABLE);
        map.put(ErrorCode.CLUSTER_ERROR, HttpResponseStatus.SERVICE_UNAVAILABLE);

        map.put(ErrorCode.TIMEOUT_ERROR, HttpResponseStatus.REQUEST_TIMEOUT);
        return Collections.unmodifiableMap(map);
    }

    public static HttpResponseStatus success(HttpMethod method) {
        if (HttpMethod.DELETE == method) {
            return HttpResponseStatus.NO_CONTENT;
        }
        if (HttpMethod.POST == method) {
            return HttpResponseStatus.CREATED;
        }
        return HttpResponseStatus.OK;
    }

    public static HttpResponseStatus error(HttpMethod method, NubeException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof HiddenException) {
            return error(method, ((HiddenException) cause).getErrorCode());
        }
        return error(method, exception.getErrorCode());
    }

    public static HttpResponseStatus error(HttpMethod method, ErrorCode errorCode) {
        HttpResponseStatus status = STATUS_ERROR.get(errorCode);
        if (Objects.nonNull(status)) {
            return status;
        }
        return STATUS_METHOD_ERROR.getOrDefault(errorCode, new HashMap<>())
                                  .getOrDefault(method, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public static ErrorCode error(HttpMethod method, int code) {
        return error(method, HttpResponseStatus.valueOf(code));
    }

    public static ErrorCode error(HttpMethod method, HttpResponseStatus statusCode) {
        return STATUS_METHOD_ERROR.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getValue()
                                                        .entrySet()
                                                        .stream()
                                                        .anyMatch(
                                                            e -> e.getKey() == method && e.getValue() == statusCode))
                                  .map(Entry::getKey)
                                  .findFirst()
                                  .orElseGet(() -> STATUS_ERROR.entrySet()
                                                               .stream()
                                                               .filter(entry -> entry.getValue() == statusCode)
                                                               .map(Entry::getKey)
                                                               .findFirst()
                                                               .orElse(ErrorCode.UNKNOWN_ERROR));
    }

}
