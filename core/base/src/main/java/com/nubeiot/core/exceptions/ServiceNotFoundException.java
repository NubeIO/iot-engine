package com.nubeiot.core.exceptions;

public class ServiceNotFoundException extends ServiceException {

    public ServiceNotFoundException(String message, Throwable e) {
        super(ErrorCode.SERVICE_NOT_FOUND, message, e);
    }

    public ServiceNotFoundException(String message) {
        this(message, null);
    }

    public ServiceNotFoundException(Throwable e) {
        this(null, e);
    }

}
