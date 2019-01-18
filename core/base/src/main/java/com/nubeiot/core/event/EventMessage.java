package com.nubeiot.core.event;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
                         @JsonProperty(value = "action", required = true) @NonNull EventAction action,
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

    private EventMessage(Status status, EventAction action, Map<String, Object> data, ErrorMessage error) {
        this(status, action, null, Objects.isNull(data) ? null : new DefaultJsonData(data), null, error);
    }

    private EventMessage(EventAction action, Status status, JsonData data, ErrorMessage error) {
        this(status, action, null, Objects.isNull(data) ? null : data.toJson().getMap(),
             Objects.isNull(data) ? DefaultJsonData.class : data.getClass(), error);
    }

    public static EventMessage error(EventAction action, Throwable throwable) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(throwable));
    }

    public static EventMessage error(EventAction action, NubeException.ErrorCode code, String message) {
        return new EventMessage(Status.FAILED, action, null, ErrorMessage.parse(code, message));
    }

    public static EventMessage initial(EventAction action) {
        return new EventMessage(Status.INITIAL, action, null, null);
    }

    public static EventMessage initial(EventAction action, JsonObject data) {
        return new EventMessage(Status.INITIAL, action, data.getMap(), null);
    }

    public static EventMessage initial(EventAction action, JsonData data) {
        return new EventMessage(action, Status.INITIAL, data, null);
    }

    public static EventMessage success(EventAction action) {
        return new EventMessage(Status.SUCCESS, action, null, null);
    }

    public static EventMessage success(EventAction action, JsonObject data) {
        return new EventMessage(Status.SUCCESS, action, data.getMap(), null);
    }

    public static EventMessage success(EventAction action, JsonData data) {
        return new EventMessage(action, Status.SUCCESS, data, null);
    }

    public static EventMessage from(Object object) {
        return JsonData.from(object, EventMessage.class, "Invalid event message format");
    }

    @JsonProperty
    public JsonObject getData() {
        return Objects.isNull(data) ? null : JsonData.from(data, dataClass).toJson();
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
