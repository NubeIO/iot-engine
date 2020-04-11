package io.github.zero.exceptions;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SneakyErrorCodeException extends SneakyException {

    @Include
    private final ErrorCode errorCode;

    public SneakyErrorCodeException(ErrorCode code, String message, Throwable e) {
        super(message, e);
        this.errorCode = code;
    }

    public SneakyErrorCodeException(ErrorCode code, String message) { this(code, message, null); }

    public SneakyErrorCodeException(ErrorCode code, Throwable e)    { this(code, null, e); }

    public SneakyErrorCodeException(String message, Throwable e)    { this(ErrorCode.UNKNOWN_ERROR, message, e); }

    public SneakyErrorCodeException(String message)                 { this(message, null); }

    public SneakyErrorCodeException(Throwable e)                    { this(ErrorCode.UNKNOWN_ERROR, null, e); }

}
