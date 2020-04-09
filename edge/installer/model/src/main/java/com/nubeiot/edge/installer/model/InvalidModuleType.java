package com.nubeiot.edge.installer.model;

import com.nubeiot.core.exceptions.NubeException;

public final class InvalidModuleType extends NubeException {

    public InvalidModuleType(String message, Throwable e) {
        super(ErrorCode.INVALID_ARGUMENT, message, e);
    }

    public InvalidModuleType(String message) {
        this(message, null);
    }

    public InvalidModuleType(Throwable e) {
        this(null, e);
    }

}
