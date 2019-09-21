package com.nubeiot.core.exceptions;

public final class TimeoutException extends NubeException {

    public TimeoutException(String message, Throwable e) { super(ErrorCode.TIMEOUT_ERROR, message, e); }

    public TimeoutException(String message)              { this(message, null); }

}
