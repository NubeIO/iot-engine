package com.nubeio.iot.share.exceptions;

import java.util.List;
import java.util.Objects;

import io.reactivex.exceptions.CompositeException;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ErrorMessage {

    private NubeException.ErrorCode code;
    private String message;

    public static ErrorMessage parse(Throwable throwable) {
        Throwable t = throwable;
        if (t instanceof CompositeException) {
            List<Throwable> exceptions = ((CompositeException) throwable).getExceptions();
            t = exceptions.get(exceptions.size() - 1);
        }
        if (t instanceof NubeException) {
            String errorMsg = Objects.nonNull(t.getCause())
                              ? t.getCause().toString()
                              : Objects.toString(t.getMessage(), t.toString());
            return new ErrorMessage(((NubeException) t).getErrorCode(), errorMsg);
        }
        return new ErrorMessage(NubeException.ErrorCode.UNKNOWN_ERROR, Objects.toString(t.getMessage(), t.toString()));
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
