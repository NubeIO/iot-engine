package com.nubeiot.core.common.utils;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @deprecated use {@link com.nubeiot.core.exceptions.HttpException}
 */
@Deprecated
public class HttpException extends RuntimeException {
    private HttpResponseStatus statusCode;
    private String message;

    public HttpException(HttpResponseStatus statusCode) {
        this.statusCode = statusCode;
    }

    public HttpException(HttpResponseStatus statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public HttpException(int statusCode) {
        this.statusCode = HttpResponseStatus.valueOf(statusCode);
    }

    public HttpException(int statusCode, String message) {
        this.statusCode = HttpResponseStatus.valueOf(statusCode);
        this.message = message;
    }

    public HttpResponseStatus getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
