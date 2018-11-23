package com.nubeiot.core.exceptions;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NubeException extends RuntimeException {

    private final ErrorCode errorCode;

    public NubeException(String message, Throwable e) {
        this(ErrorCode.UNKNOWN_ERROR, message, e);
    }

    public NubeException(String message) { this(message, null); }

    public NubeException(Throwable e)    { this(null, e); }

    public NubeException(ErrorCode code, String message, Throwable e) {
        super(message, e);
        this.errorCode = code;
    }

    public NubeException(ErrorCode code, String message) {
        this(code, message, null);
    }

    @Getter
    public enum ErrorCode implements Serializable {

        INVALID_ARGUMENT,
        ALREADY_EXIST,
        NOT_FOUND,
        SECURITY_ERROR,
        AUTHENTICATION_ERROR,
        INSUFFICIENT_PERMISSION_ERROR,
        HTTP_ERROR,
        SERVICE_ERROR,
        ENGINE_ERROR,
        DATABASE_ERROR,
        STATE_ERROR,
        UNKNOWN_ERROR

    }

}
