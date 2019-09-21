package com.nubeiot.core.exceptions;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class NubeException extends RuntimeException {

    @Include
    private final ErrorCode errorCode;

    public NubeException(ErrorCode code, String message, Throwable e) {
        super(message, e);
        this.errorCode = code;
    }

    public NubeException(ErrorCode code, String message) { this(code, message, null); }

    public NubeException(ErrorCode code, Throwable e)    { this(code, null, e); }

    public NubeException(String message, Throwable e)    { this(ErrorCode.UNKNOWN_ERROR, message, e); }

    public NubeException(String message)                 { this(message, null); }

    public NubeException(Throwable e)                    { this(ErrorCode.UNKNOWN_ERROR, null, e); }

    @Getter
    public enum ErrorCode implements Serializable {

        DESIRED_ERROR,
        INVALID_ARGUMENT,
        ALREADY_EXIST,
        NOT_FOUND,
        SECURITY_ERROR,
        AUTHENTICATION_ERROR,
        INSUFFICIENT_PERMISSION_ERROR,
        HTTP_ERROR,
        SERVICE_ERROR,
        INITIALIZER_ERROR,
        ENGINE_ERROR,
        CLUSTER_ERROR,
        EVENT_ERROR,
        DATABASE_ERROR,
        STATE_ERROR,
        TIMEOUT_ERROR,
        NETWORK_ERROR,
        UNKNOWN_ERROR
    }

}
