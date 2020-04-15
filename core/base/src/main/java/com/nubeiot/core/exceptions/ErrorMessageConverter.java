package com.nubeiot.core.exceptions;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.github.zero.utils.Strings;

import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;

/**
 * @see ErrorMessage
 * @see NubeException
 */
@SuppressWarnings("unchecked")
@Builder(access = AccessLevel.PRIVATE)
public final class ErrorMessageConverter<T extends NubeException> implements Function<ErrorMessage, T> {

    private final ErrorCode code;
    private final String overrideMsg;

    public static NubeException from(@NonNull ErrorMessage error) {
        return ErrorMessageConverter.builder().build().apply(error);
    }

    public static NubeException from(@NonNull ErrorMessage error, ErrorCode errorCode, String overrideMsg) {
        return ErrorMessageConverter.builder().code(errorCode).overrideMsg(overrideMsg).build().apply(error);
    }

    @Override
    public T apply(@NonNull ErrorMessage error) {
        if (Objects.nonNull(error.getThrowable())) {
            return (T) error.getThrowable();
        }
        String msg = Strings.isBlank(overrideMsg)
                     ? error.getMessage()
                     : Strings.format("{0} | Error: {1}", overrideMsg, error.getCode());
        return (T) new NubeException(Optional.ofNullable(code).orElse(error.getCode()), msg);
    }

}
