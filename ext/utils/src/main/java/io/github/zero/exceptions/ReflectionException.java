package io.github.zero.exceptions;

public class ReflectionException extends SneakyErrorCodeException {

    public ReflectionException(String message, Throwable e) {
        super(ErrorCode.REFLECTION_ERROR, message, e);
    }

    public ReflectionException(String message) {
        this(message, null);
    }

    public ReflectionException(Throwable e) {
        this(null, e);
    }

}
