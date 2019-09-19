package com.nubeiot.core.exceptions;

public class DesiredException extends NubeException {

    public DesiredException(String message, Throwable e) {
        super(ErrorCode.DESIRED_ERROR, message, e);
    }

    public DesiredException(String message) {
        super(ErrorCode.DESIRED_ERROR, message);
    }

    public DesiredException(Throwable e) {
        super(ErrorCode.DESIRED_ERROR, e);
    }

}
