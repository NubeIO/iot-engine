package com.nubeiot.edge.connector.bacnet.discover;

import java.util.Objects;
import java.util.Optional;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.dto.RequestData;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DiscoverRequest.Builder.class)
@FieldNameConstants(level = AccessLevel.PUBLIC)
public final class DiscoverRequest implements JsonData {

    private JsonObject network;
    private String networkCode;
    private Integer deviceInstance;
    private String objectCode;

    @NonNull
    public static DiscoverRequest from(@NonNull RequestData requestData, @NonNull DiscoverLevel level) {
        return from(Optional.ofNullable(requestData.body()).orElse(new JsonObject()), level);
    }

    @NonNull
    public static DiscoverRequest from(@NonNull JsonObject body, @NonNull DiscoverLevel level) {
        return validate(JsonData.from(body, DiscoverRequest.class), level);
    }

    @NonNull
    public static DiscoverRequest validate(@NonNull DiscoverRequest request, @NonNull DiscoverLevel level) {
        if (DiscoverLevel.NETWORK.mustValidate(level)) {
            Strings.requireNotBlank(request.getNetworkCode(), "Missing BACnet network code");
        }
        if (DiscoverLevel.DEVICE.mustValidate(level)) {
            Objects.requireNonNull(request.getDeviceInstance(), "Missing BACnet device code");
        }
        if (DiscoverLevel.OBJECT.mustValidate(level)) {
            Strings.requireNotBlank(request.getObjectCode(), "Missing BACnet object code");
        }
        return request;
    }

    @JsonIgnore
    public ObjectIdentifier getDeviceCode() {
        return Optional.ofNullable(deviceInstance)
                       .map(number -> new ObjectIdentifier(ObjectType.device, number))
                       .orElse(null);
    }

    @JsonIgnore
    public ObjectIdentifier getObjectId() {
        return Optional.ofNullable(objectCode).map(code -> ObjectIdentifierMixin.deserialize(objectCode)).orElse(null);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum DiscoverLevel {

        NETWORK(1), DEVICE(2), OBJECT(3);

        private final int level;

        boolean mustValidate(@NonNull DiscoverLevel given) {
            return this.level <= given.level;
        }
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
