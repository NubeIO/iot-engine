package io.nubespark.events;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.nubespark.exceptions.NubeException;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMessage {

    @Getter
    private Status status;
    @Getter
    private String action;
    private Map<String, Object> data;
    @Getter
    @JsonProperty
    private ErrorMessage error;

    private EventMessage(Status status, String action, JsonObject data, ErrorMessage error) {
        this.status = Objects.requireNonNull(status);
        this.action = Objects.requireNonNull(action);
        this.data = Objects.isNull(data) ? new HashMap<>() : data.mapTo(Map.class);
        this.error = error;
    }

    public static EventMessage error(String action, Throwable throwable) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(String action, NubeException.ErrorCode code, String message) {
        return new EventMessage(Status.FAILED, action, null, new ErrorMessage(code, message));
    }

    public static EventMessage success(String action) {
        return new EventMessage(Status.OK, action, null, null);
    }

    public static EventMessage success(String action, JsonObject data) {
        return new EventMessage(Status.OK, action, data, null);
    }

    public static EventMessage success(String action, Object data) {
        return new EventMessage(Status.OK, action, JsonObject.mapFrom(data), null);
    }

    public static EventMessage from(Object object) {
        return JsonObject.mapFrom(Objects.requireNonNull(object)).mapTo(EventMessage.class);
    }

    public JsonObject getData() {
        return JsonObject.mapFrom(this.data);
    }

    @JsonIgnore
    public boolean isOk() {
        return this.status == Status.OK;
    }

    @JsonIgnore
    public boolean isError() {
        return this.status == Status.FAILED;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    public enum Status implements Serializable {
        OK, FAILED
    }


    @ToString
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorMessage {

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
            return new ErrorMessage(NubeException.ErrorCode.UNKNOWN_ERROR,
                                    Objects.toString(t.getMessage(), t.toString()));
        }

        public JsonObject toJson() {
            return JsonObject.mapFrom(this);
        }

    }

}
