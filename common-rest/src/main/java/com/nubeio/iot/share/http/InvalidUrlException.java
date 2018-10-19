package com.nubeio.iot.share.http;

import com.nubeio.iot.share.exceptions.NubeException;

public class InvalidUrlException extends NubeException {

    public InvalidUrlException(String message, Throwable e) {
        super(ErrorCode.HTTP_ERROR, message, e);
    }

    public InvalidUrlException(String message) {
        this(message, null);
    }

    public InvalidUrlException(Throwable e) {
        this(null, e);
    }

}
