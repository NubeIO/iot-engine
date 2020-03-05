package com.nubeiot.core.exceptions;

public class DatabaseException extends NubeException {

    protected DatabaseException(ErrorCode errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }

    public DatabaseException(String message, Throwable e) {
        this(ErrorCode.DATABASE_ERROR, message, e);
    }

    public DatabaseException(String message) { this(message, null); }

    public DatabaseException(Throwable e)    { this(null, e); }

    public static final class TransactionalException extends DatabaseException {

        public TransactionalException(Throwable e) {
            super(ErrorCode.TRANSACTION_ERROR, null, e);
        }

    }

}
