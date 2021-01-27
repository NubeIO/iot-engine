package com.nubeiot.edge.connector.bacnet.discovery;

import java.util.Objects;
import java.util.Optional;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.serotonin.bacnet4j.type.enumerated.ObjectType;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@FieldNameConstants
@Builder(builderClassName = "Builder")
public final class DiscoveryRequest implements JsonData {

    private final JsonObject network;
    private final String networkCode;
    private final Integer deviceInstance;
    private final String objectCode;

    @NonNull
    public static DiscoveryRequest from(@NonNull RequestData requestData, @NonNull DiscoveryLevel level) {
        return from(Optional.ofNullable(requestData.body()).orElse(new JsonObject()), level);
    }

    @NonNull
    public static DiscoveryRequest from(@NonNull JsonObject body, @NonNull DiscoveryLevel level) {
        return validate(JsonData.from(body, DiscoveryRequest.class, JsonData.LENIENT_MAPPER), level);
    }

    @NonNull
    public static DiscoveryRequest validate(@NonNull DiscoveryRequest request, @NonNull DiscoveryLevel level) {
        if (DiscoveryLevel.NETWORK.mustValidate(level)) {
            Strings.requireNotBlank(request.getNetworkCode(), "Missing BACnet network code");
        }
        if (DiscoveryLevel.DEVICE.mustValidate(level)) {
            Objects.requireNonNull(request.getDeviceInstance(), "Missing BACnet device code");
        }
        if (DiscoveryLevel.OBJECT.mustValidate(level)) {
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

}
