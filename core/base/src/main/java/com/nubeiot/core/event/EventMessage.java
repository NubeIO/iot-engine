package com.nubeiot.core.event;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.HiddenException;
import com.nubeiot.core.exceptions.NubeException;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Represents for data transfer object in event bus system.
 *
 * @see Status
 * @see EventAction
 * @see ErrorMessage
 */
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class EventMessage implements Serializable {

    @Getter
    private final Status status;
    @Getter
    private final EventAction action;
    @Getter
    private final EventAction prevAction;
    private final Map<String, Object> data;
    @Getter
    @JsonProperty
    private final ErrorMessage error;

    @JsonCreator
    private EventMessage(@JsonProperty(value = "status", defaultValue = "SUCCESS") Status status,
                         @JsonProperty(value = "action", required = true) @NonNull EventAction action,
                         @JsonProperty(value = "prevAction") EventAction prevAction,
                         @JsonProperty(value = "data") Map<String, Object> data,
                         @JsonProperty(value = "error") ErrorMessage error) {
        this.status = Objects.isNull(status) ? Status.SUCCESS : status;
        this.action = action;
        this.prevAction = prevAction;
        this.data = data;
        this.error = error;
    }

    private EventMessage(Status status, EventAction action, Map<String, Object> data, ErrorMessage error) {
        this(status, action, null, data, error);
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
        return new EventMessage(Status.SUCCESS, action, data.getMap(), null);
    }

    public static EventMessage success(EventAction action, Object data) {
        return new EventMessage(Status.SUCCESS, action, JsonObject.mapFrom(data).getMap(), null);
    }

    public static EventMessage from(Object object) {
        try {
            JsonObject entries = object instanceof String
                                 ? new JsonObject((String) object)
                                 : JsonObject.mapFrom(Objects.requireNonNull(object));
            return entries.mapTo(EventMessage.class);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new NubeException(NubeException.ErrorCode.INVALID_ARGUMENT, "Invalid event message format",
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
