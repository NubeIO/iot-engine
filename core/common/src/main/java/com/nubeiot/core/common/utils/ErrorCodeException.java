package com.nubeiot.core.common.utils;

import com.nubeiot.core.exceptions.NubeException;

/**
 * @deprecated use {@link NubeException} and its sub classes
 */
@Deprecated
public class ErrorCodeException extends RuntimeException {
    private ErrorCodes errorCodes;

    public ErrorCodeException(ErrorCodes errorCodes) {
        this.errorCodes = errorCodes;
    }

    public ErrorCodes getErrorCodes() {
        return errorCodes;
    }
}
