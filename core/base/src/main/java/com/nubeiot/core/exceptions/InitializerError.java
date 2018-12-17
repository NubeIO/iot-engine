package com.nubeiot.core.exceptions;

public class InitializerError extends NubeException {

    public InitializerError(String message, Throwable e) {
        super(ErrorCode.INITIALIZER_ERROR, message, e);
    }

    public InitializerError(String message) {
        this(message, null);
    }

    public InitializerError(Throwable e) {
        this(null, e);
    }

}
