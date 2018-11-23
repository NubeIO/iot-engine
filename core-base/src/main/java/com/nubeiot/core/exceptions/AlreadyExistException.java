package com.nubeiot.core.exceptions;

public class AlreadyExistException extends NubeException {

    public AlreadyExistException(String message, Throwable e) {
        super(ErrorCode.ALREADY_EXIST, message, e);
    }

    public AlreadyExistException(String message) {
        this(message, null);
    }

    public AlreadyExistException(Throwable e) {
        this(null, e);
    }

}
