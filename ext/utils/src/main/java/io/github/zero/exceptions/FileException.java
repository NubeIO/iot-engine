package io.github.zero.exceptions;

public class FileException extends SneakyErrorCodeException {

    public FileException(String message, Throwable e) {
        super(ErrorCode.FILE_ERROR, message, e);
    }

    public FileException(String message) {
        this(message, null);
    }

    public FileException(Throwable e) {
        this(null, e);
    }

}
