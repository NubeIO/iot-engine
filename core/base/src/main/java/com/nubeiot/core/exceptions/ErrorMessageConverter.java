package com.nubeiot.core.exceptions;

import java.util.Objects;
import java.util.function.Function;

import lombok.NonNull;

/**
 * @see ErrorMessage
 * @see NubeException
 */
@SuppressWarnings("unchecked")
public final class ErrorMessageConverter<T extends NubeException> implements Function<ErrorMessage, T> {

    @Override
    public T apply(@NonNull ErrorMessage errorMessage) {
        if (Objects.nonNull(errorMessage.getThrowable())) {
            return (T) errorMessage.getThrowable();
        }
        return (T) new NubeException(errorMessage.getCode(), errorMessage.getMessage());
    }

}
