package io.github.zero.exceptions;

public final class InvalidUrlException extends SneakyErrorCodeException {

    public InvalidUrlException(String message, Throwable e) {
        super(ErrorCode.URL_ERROR, message, e);
    }

    public InvalidUrlException(String message) {
        this(message, null);
    }

    public InvalidUrlException(Throwable e) {
        this(null, e);
    }

}
