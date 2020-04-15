package io.github.zero.exceptions;

import lombok.NonNull;

public class HiddenException extends SneakyErrorCodeException {

    public HiddenException(ErrorCode errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }

    public HiddenException(String message, Throwable e) {
        this(ErrorCode.HIDDEN, message, e);
    }

    public HiddenException(String message) {
        this(message, null);
    }

    public HiddenException(Throwable e) {
        this(null, e);
    }

    public HiddenException(@NonNull SneakyErrorCodeException e) {
        this(e.getErrorCode(), e.getMessage(), e);
    }

}
