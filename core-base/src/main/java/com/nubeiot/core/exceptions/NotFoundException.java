package com.nubeiot.core.exceptions;

public class NotFoundException extends NubeException {

    public NotFoundException(String message, Throwable e) {
        super(ErrorCode.NOT_FOUND, message, e);
    }

    public NotFoundException(String message) {
        this(message, null);
    }

    public NotFoundException(Throwable e) {
        this(null, e);
    }

}
