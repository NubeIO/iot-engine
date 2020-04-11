package io.github.zero.exceptions;

/**
 * Represents for Sneaky exception that stands for uncaught exception or runtime exception
 *
 * @since 1.0.0
 */
public class SneakyException extends RuntimeException {

    public SneakyException() {
        super();
    }

    public SneakyException(String message) {
        super(message);
    }

    public SneakyException(String message, Throwable cause) {
        super(message, cause);
    }

    public SneakyException(Throwable cause) {
        super(cause);
    }

    protected SneakyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
