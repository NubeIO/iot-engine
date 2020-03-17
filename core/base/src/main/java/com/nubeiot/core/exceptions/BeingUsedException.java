package com.nubeiot.core.exceptions;

public final class BeingUsedException extends NubeException {

    public BeingUsedException(String message, Throwable e) {
        super(ErrorCode.BEING_USED, message, e);
    }

    public BeingUsedException(String message) {
        this(message, null);
    }

    public BeingUsedException(Throwable e) {
        this(null, e);
    }

}
