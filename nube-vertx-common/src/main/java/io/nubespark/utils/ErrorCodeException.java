package io.nubespark.utils;

public class ErrorCodeException extends RuntimeException {
    private ErrorCodes errorCodes;

    public ErrorCodeException(ErrorCodes errorCodes) {
        this.errorCodes = errorCodes;
    }

    public ErrorCodes getErrorCodes() {
        return errorCodes;
    }
}
