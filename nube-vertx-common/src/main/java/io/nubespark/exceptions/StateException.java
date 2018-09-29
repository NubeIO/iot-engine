package io.nubespark.exceptions;

public final class StateException extends NubeException {

    public StateException(String message, Throwable e) {
        super(ErrorCode.STATE_ERROR, message, e);
    }

    public StateException(String message) { this(message, null); }

    public StateException(Throwable e)    { this(null, e); }

}
