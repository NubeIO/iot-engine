package com.nubeiot.core.exceptions;

public class InitializerError extends NubeException {

    public InitializerError(String message, Throwable e) {
        super(ErrorCode.INITIALIZER_ERROR, message, e);
    }

    public InitializerError(String message) { this(message, null);}

    public InitializerError(Throwable e)    { this(null, e); }

    public static final class MigrationError extends InitializerError {

        public MigrationError(String message, Throwable e) {
            super(message, e);
        }

        public MigrationError(String message) { this(message, null); }

        public MigrationError(Throwable e)    { this(null, e); }

    }

}
