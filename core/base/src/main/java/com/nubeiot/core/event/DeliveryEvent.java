package com.nubeiot.core.event;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.exceptions.NubeException.ErrorCode;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * It bundles all information for single event to delivery
 */
@Getter
@EqualsAndHashCode(doNotUseGetters = true, onlyExplicitlyIncluded = true)
@Builder(builderClassName = "Builder")
@ToString(onlyExplicitlyIncluded = true)
@JsonDeserialize(builder = DeliveryEvent.Builder.class)
public final class DeliveryEvent implements JsonData {

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private final String address;

    @Default
    @EqualsAndHashCode.Include
    @ToString.Include
    private final EventPattern pattern = EventPattern.REQUEST_RESPONSE;

    @NonNull
    @EqualsAndHashCode.Include
    @ToString.Include
    private final EventAction action;

    private final JsonObject payload;

    public static DeliveryEvent from(EventModel model, EventAction action) {
        if (!model.getEvents().contains(action)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Action must match one of EventModel Actions");
        }
        return new DeliveryEvent(model.getAddress(), model.getPattern(), action, null);
    }

    public static DeliveryEvent from(EventModel model, EventAction action, RequestData payload) {
        return from(model, action, payload.toJson());
    }

    public static DeliveryEvent from(EventModel model, EventAction action, JsonObject payload) {
        if (!model.getEvents().contains(action)) {
            throw new NubeException(ErrorCode.INVALID_ARGUMENT, "Action must match one of EventModel Actions");
        }
        return new DeliveryEvent(model.getAddress(), model.getPattern(), action, payload);
    }

    public EventMessage payload() {
        return EventMessage.initial(action, payload);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
