package com.nubeiot.core.exceptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.vertx.core.json.JsonObject;
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
public final class ErrorMessage implements Serializable {

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
        return new ErrorMessage(new NubeExceptionConverter().apply(throwable));
    }

    public static ErrorMessage parse(@NonNull NubeException.ErrorCode code, @NonNull String message) {
        return new ErrorMessage(code, message);
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
