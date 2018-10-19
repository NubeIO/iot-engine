package com.nubeio.iot.share.exceptions;

public final class DatabaseException extends NubeException {

    public DatabaseException(String message, Throwable e) {
        super(ErrorCode.DATABASE_ERROR, message, e);
    }

    public DatabaseException(String message) { this(message, null); }

    public DatabaseException(Throwable e)    { this(null, e); }

}
