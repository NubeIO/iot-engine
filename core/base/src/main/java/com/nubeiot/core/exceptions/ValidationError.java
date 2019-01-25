package com.nubeiot.core.exceptions;

import lombok.NonNull;

public class ValidationError extends NubeException {

    public ValidationError(@NonNull ErrorCode code, @NonNull String message) {
        super(code, message);
    }

    public ValidationError(@NonNull String message) {
        super(ErrorCode.INVALID_ARGUMENT, message);
    }

}
