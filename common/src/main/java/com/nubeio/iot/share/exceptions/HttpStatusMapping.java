package com.nubeio.iot.share.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpStatusMapping {

    public static HttpResponseStatus success(io.vertx.core.http.HttpMethod method) {
        if (HttpMethod.DELETE == method) {
            return HttpResponseStatus.NO_CONTENT;
        }
        if (HttpMethod.POST == method || HttpMethod.PUT == method) {
            return HttpResponseStatus.CREATED;
        }
        return HttpResponseStatus.OK;
    }

    public static HttpResponseStatus error(HttpMethod method, NubeException exception) {
        return error(method, exception.getErrorCode());
    }

    //    TODO need more update
    public static HttpResponseStatus error(HttpMethod method, NubeException.ErrorCode errorCode) {
        if (NubeException.ErrorCode.INVALID_ARGUMENT == errorCode || NubeException.ErrorCode.HTTP_ERROR == errorCode) {
            return HttpResponseStatus.BAD_REQUEST;
        }
        if (NubeException.ErrorCode.NOT_FOUND == errorCode) {
            if (HttpMethod.GET == method) {
                return HttpResponseStatus.NOT_FOUND;
            }
            return HttpResponseStatus.GONE;
        }
        if (NubeException.ErrorCode.ALREADY_EXIST == errorCode) {
            return HttpResponseStatus.CONFLICT;
        }
        if (NubeException.ErrorCode.AUTHENTICATION_ERROR == errorCode) {
            return HttpResponseStatus.UNAUTHORIZED;
        }
        if (NubeException.ErrorCode.INSUFFICIENT_PERMISSION_ERROR == errorCode) {
            return HttpResponseStatus.FORBIDDEN;
        }
        if (NubeException.ErrorCode.STATE_ERROR == errorCode) {
            return HttpResponseStatus.CONFLICT;
        }
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

}
