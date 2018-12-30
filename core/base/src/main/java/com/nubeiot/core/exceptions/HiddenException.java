package com.nubeiot.core.exceptions;

import lombok.NonNull;

/**
 * Hides technical error for end user, but for product logging.
 */
public class HiddenException extends NubeException {

    public HiddenException(@NonNull Throwable e) {
        super(e);
    }

    public HiddenException(@NonNull ErrorCode code, @NonNull Throwable e) {
        this(code, null, e);
    }

    public HiddenException(@NonNull ErrorCode code, @NonNull String message) {
        this(code, message, null);
    }

    public HiddenException(@NonNull ErrorCode code, String message, Throwable e) {
        super(code, message, e);
    }

    public static final class ImplementationError extends HiddenException {

        public ImplementationError(@NonNull ErrorCode code, String message, Throwable e) {
            super(code, message, e);
        }

        public ImplementationError(@NonNull ErrorCode code, @NonNull String message) {
            super(code, message);
        }

    }

}
