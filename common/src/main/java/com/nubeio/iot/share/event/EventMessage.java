package com.nubeio.iot.share.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeio.iot.share.enums.Status;
import com.nubeio.iot.share.exceptions.ErrorMessage;
import com.nubeio.iot.share.exceptions.NubeException;

import io.vertx.core.json.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventMessage {

    @Getter
    private Status status;
    @Getter
    private EventType action;
    private Map<String, Object> data;
    @Getter
    @JsonProperty
    private ErrorMessage error;

    private EventMessage(Status status, EventType action, JsonObject data, ErrorMessage error) {
        this.status = Objects.requireNonNull(status);
        this.action = Objects.requireNonNull(action);
        this.data = Objects.isNull(data) ? new HashMap<>() : data.mapTo(Map.class);
        this.error = error;
    }

    public static EventMessage error(EventType action, Throwable throwable) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(EventType action, NubeException.ErrorCode code, String message) {
        return new EventMessage(Status.FAILED, action, null, new ErrorMessage(code, message));
    }

    public static EventMessage success(EventType action) {
        return new EventMessage(Status.SUCCESS, action, null, null);
    }

    public static EventMessage success(EventType action, JsonObject data) {
        return new EventMessage(Status.SUCCESS, action, data, null);
    }

    public static EventMessage success(EventType action, Object data) {
        return new EventMessage(Status.SUCCESS, action, JsonObject.mapFrom(data), null);
    }

    public static EventMessage from(Object object) {
        return JsonObject.mapFrom(Objects.requireNonNull(object)).mapTo(EventMessage.class);
    }

    public JsonObject getData() {
        return JsonObject.mapFrom(this.data);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    @JsonIgnore
    public boolean isError() {
        return this.status == Status.FAILED;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

}
