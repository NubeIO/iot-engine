package com.nubeiot.core.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @see NubeException.ErrorCode
 */
//    TODO need more update
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpStatusMapping {

    private static final Map<NubeException.ErrorCode, HttpResponseStatus> STATUS_ERROR = init();
    private static final Map<NubeException.ErrorCode, Map<HttpMethod, HttpResponseStatus>> STATUS_METHOD_ERROR
            = initMethod();

    private static Map<NubeException.ErrorCode, Map<HttpMethod, HttpResponseStatus>> initMethod() {
        Map<NubeException.ErrorCode, Map<HttpMethod, HttpResponseStatus>> map = new HashMap<>();

        Map<HttpMethod, HttpResponseStatus> notFound = new HashMap<>();
        Arrays.stream(HttpMethod.values()).forEach(method -> notFound.put(method, HttpResponseStatus.GONE));
        notFound.put(HttpMethod.GET, HttpResponseStatus.NOT_FOUND);
        map.put(NubeException.ErrorCode.NOT_FOUND, notFound);

        return Collections.unmodifiableMap(map);
    }

    private static Map<NubeException.ErrorCode, HttpResponseStatus> init() {
        Map<NubeException.ErrorCode, HttpResponseStatus> map = new HashMap<>();
        map.put(NubeException.ErrorCode.INVALID_ARGUMENT, HttpResponseStatus.BAD_REQUEST);
        map.put(NubeException.ErrorCode.HTTP_ERROR, HttpResponseStatus.BAD_REQUEST);

        map.put(NubeException.ErrorCode.ALREADY_EXIST, HttpResponseStatus.CONFLICT);
        map.put(NubeException.ErrorCode.STATE_ERROR, HttpResponseStatus.CONFLICT);

        map.put(NubeException.ErrorCode.AUTHENTICATION_ERROR, HttpResponseStatus.UNAUTHORIZED);
        map.put(NubeException.ErrorCode.SECURITY_ERROR, HttpResponseStatus.FORBIDDEN);
        map.put(NubeException.ErrorCode.INSUFFICIENT_PERMISSION_ERROR, HttpResponseStatus.FORBIDDEN);

        map.put(NubeException.ErrorCode.EVENT_ERROR, HttpResponseStatus.SERVICE_UNAVAILABLE);
        map.put(NubeException.ErrorCode.CLUSTER_ERROR, HttpResponseStatus.SERVICE_UNAVAILABLE);

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

    public static HttpResponseStatus error(HttpMethod method, NubeException.ErrorCode errorCode) {
        HttpResponseStatus status = STATUS_ERROR.get(errorCode);
        if (Objects.nonNull(status)) {
            return status;
        }
        return STATUS_METHOD_ERROR.getOrDefault(errorCode, new HashMap<>())
                                  .getOrDefault(method, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

}
