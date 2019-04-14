package com.nubeiot.core.event;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.enums.Status;
import com.nubeiot.core.exceptions.ErrorMessage;
import com.nubeiot.core.exceptions.NubeException;

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
public final class EventMessage implements Serializable, JsonData {

    @Getter
    private final Status status;
    @Getter
    private final EventAction action;
    @Getter
    private final EventAction prevAction;
    private final Map<String, Object> data;
    @JsonIgnore
    private final Class<? extends JsonData> dataClass;
    @Getter
    @JsonProperty
    private final ErrorMessage error;

    @JsonCreator
    private EventMessage(@JsonProperty(value = "status", defaultValue = "INITIAL") Status status,
                         @NonNull @JsonProperty(value = "action", required = true) EventAction action,
                         @JsonProperty(value = "prevAction") EventAction prevAction,
                         @JsonProperty(value = "data") Map<String, Object> data,
                         @JsonProperty(value = "dataClass") Class<? extends JsonData> dataClass,
                         @JsonProperty(value = "error") ErrorMessage error) {
        this.status = Objects.isNull(status) ? Status.INITIAL : status;
        this.action = action;
        this.prevAction = prevAction;
        this.data = data;
        this.dataClass = Objects.isNull(dataClass) ? DefaultJsonData.class : dataClass;
        this.error = error;
    }

    private EventMessage(EventAction action, Status status) {
        this(status, action, null, null, null, null);
    }

    private EventMessage(EventAction action, Status status, @NonNull ErrorMessage error) {
        this(status, action, null, null, null, error);
    }

    private EventMessage(EventAction action, Status status, @NonNull JsonData data) {
        this(status, action, null, data.toJson().getMap(), data.getClass(), null);
    }

    private EventMessage(EventAction action, Status status, @NonNull JsonObject data) {
        this(status, action, null, data.getMap(), null, null);
    }

    public static EventMessage error(EventAction action, @NonNull Throwable throwable) {
        return new EventMessage(action, Status.FAILED, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(EventAction action, @NonNull NubeException.ErrorCode code,
                                     @NonNull String message) {
        return new EventMessage(action, Status.FAILED, ErrorMessage.parse(code, message));
    }

    public static EventMessage initial(EventAction action) {
        return new EventMessage(action, Status.INITIAL);
    }

    public static EventMessage initial(EventAction action, JsonObject data) {
        return new EventMessage(action, Status.INITIAL, data);
    }

    public static EventMessage initial(EventAction action, JsonData data) {
        return new EventMessage(action, Status.INITIAL, data);
    }

    public static EventMessage success(EventAction action) {
        return new EventMessage(action, Status.SUCCESS);
    }

    public static EventMessage success(EventAction action, JsonObject data) {
        return new EventMessage(action, Status.SUCCESS, data);
    }

    public static EventMessage success(EventAction action, JsonData data) {
        return new EventMessage(action, Status.SUCCESS, data);
    }

    public static EventMessage from(@NonNull Status status, @NonNull EventAction action, EventAction prevAction,
                                    ErrorMessage message) {
        return new EventMessage(status, action, prevAction, null, null, message);
    }

    public static EventMessage from(@NonNull Status status, @NonNull EventAction action, EventAction prevAction,
                                    JsonObject data) {
        return new EventMessage(action, status, data);
    }

    /**
     * Try parse given object to {@link EventMessage}
     *
     * @param object any non null object
     * @return event message instance
     * @throws NubeException if wrong format
     */
    public static EventMessage tryParse(@NonNull Object object) {
        return tryParse(object, false);
    }

    /**
     * Try parse with fallback data
     *
     * @param object  any non null object
     * @param lenient {@code true} if want to force given object to data json with {@code action} is {@link
     *                EventAction#UNKNOWN}
     * @return event message instance
     */
    public static EventMessage tryParse(@NonNull Object object, boolean lenient) {
        try {
            return JsonData.from(object, EventMessage.class, "Invalid event message format");
        } catch (NubeException e) {
            if (lenient) {
                return EventMessage.initial(EventAction.UNKNOWN, JsonData.tryParse(object));
            }
            throw e;
        }
    }

    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty
    public JsonObject getData() {
        return Objects.isNull(data) ? null : JsonData.from(data, dataClass).toJson();
    }

    @SuppressWarnings("unchecked")
    public <T> T data() {
        return Objects.isNull(data) ? null : (T) JsonData.from(data, dataClass);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return this.status == Status.SUCCESS;
    }

    @JsonIgnore
    public boolean isError() {
        return this.status == Status.FAILED;
    }

}
