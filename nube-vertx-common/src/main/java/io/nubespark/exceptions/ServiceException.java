package io.nubespark.exceptions;

public final class ServiceException extends NubeException {

    public ServiceException(String message, Throwable e) {
        super(ErrorCode.SERVICE_ERROR, message, e);
    }

    public ServiceException(String message) { this(message, null); }

    public ServiceException(Throwable e)    { this(null, e); }

}
