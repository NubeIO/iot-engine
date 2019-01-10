package com.nubeiot.core.exceptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.core.dto.JsonData;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * The error message.
 */
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage implements Serializable, JsonData {

    @JsonIgnore
    private NubeException throwable;
    private NubeException.ErrorCode code;
    private String message;

    private ErrorMessage(@NonNull NubeException throwable) {
        this.throwable = throwable;
        this.code = throwable.getErrorCode();
        this.message = throwable.getMessage();
    }

    private ErrorMessage(@NonNull NubeException.ErrorCode code, @NonNull String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorMessage parse(@NonNull Throwable throwable) {
        return new ErrorMessage(NubeExceptionConverter.friendly(throwable));
    }

    public static ErrorMessage parse(@NonNull NubeException.ErrorCode code, @NonNull String message) {
        return new ErrorMessage(code, message);
    }

}
