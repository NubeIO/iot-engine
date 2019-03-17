package com.nubeiot.core.http.base;

import com.nubeiot.core.exceptions.NubeException;

public final class InvalidUrlException extends NubeException {

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
