package com.nubeio.iot.share.exceptions;

public final class HttpException extends NubeException {

    public HttpException(String message, Throwable e) {
        super(ErrorCode.HTTP_ERROR, message, e);
    }

    public HttpException(String message) { this(message, null); }

    public HttpException(Throwable e)    { this(null, e); }

}
