package com.nubeiot.core.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents for data transfer object in event bus system.
 *
 * @see Status
 * @see EventAction
 * @see ErrorMessage
 */
@ToString
public final class EventMessage implements Serializable {

    @Getter
    private final Status status;
    @Getter
    private final EventAction action;
    private final Map<String, Object> data;
    @Getter
    @JsonProperty
    private final ErrorMessage error;

    EventMessage() {
        this(Status.SUCCESS, null, null, null);
    }

    private EventMessage(Status status, EventAction action, JsonObject data, ErrorMessage error) {
        this.status = Objects.isNull(status) ? Status.SUCCESS : status;
        this.action = action;
        this.data = Objects.isNull(data) ? new HashMap<>() : data.getMap();
        this.error = error;
    }

    public static EventMessage error(EventAction action, Throwable throwable) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(EventAction action, NubeException.ErrorCode code, String message) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(code, message));
    }

    public static EventMessage success(EventAction action) {
        return new EventMessage(Status.SUCCESS, action, null, null);
    }

    public static EventMessage success(EventAction action, JsonObject data) {
        return new EventMessage(Status.SUCCESS, action, data, null);
    }

    public static EventMessage success(EventAction action, Object data) {
        return new EventMessage(Status.SUCCESS, action, JsonObject.mapFrom(data), null);
    }

    public static EventMessage from(Object object) {
        try {
            return JsonObject.mapFrom(Objects.requireNonNull(object)).mapTo(EventMessage.class);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Message format is not correct",
                                    new HiddenException(ex));
        }
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
