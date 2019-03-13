package com.nubeiot.core.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

import lombok.Getter;
import lombok.NonNull;

public final class HttpException extends NubeException {

    @Getter
    private HttpResponseStatus statusCode = null;

    public HttpException(String message, Throwable e) {
        super(ErrorCode.HTTP_ERROR, message, e);
    }

    public HttpException(int statusCode, String message) {
        this(HttpResponseStatus.valueOf(statusCode), message);
    }

    public HttpException(@NonNull HttpResponseStatus statusCode, String message) {
        super(ErrorCode.HTTP_ERROR, message);
        this.statusCode = statusCode;
    }

    public HttpException(String message) { this(message, null); }

    public HttpException(Throwable e)    { this(null, e); }

}
