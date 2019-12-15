package com.nubeiot.edge.connector.bacnet.mixin;

import java.util.Objects;
import java.util.Optional;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(builderClassName = "Builder")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RemoteDeviceMixin implements BACnetMixin {

    @JsonIgnore
    private final ObjectIdentifier objectId;
    private final int instanceNumber;
    private final String name;
    private final AddressMixin address;
    @JsonIgnore
    private final PropertyValuesMixin propertyValues;

    public static RemoteDeviceMixin create(@NonNull RemoteDevice device) {
        return create(device, null);
    }

    public static RemoteDeviceMixin create(@NonNull RemoteDevice device, PropertyValuesMixin values) {
        return create(device.getObjectIdentifier(), device.getName(), device.getAddress(), values);
    }

    public static RemoteDeviceMixin create(@NonNull ObjectIdentifier objectId, String name, @NonNull Address address,
                                           @NonNull PropertyValuesMixin values) {
        return new RemoteDeviceMixin(objectId, objectId.getInstanceNumber(), name, AddressMixin.create(address),
                                     values);
    }

    public static RemoteDeviceMixin create(@NonNull ObjectIdentifier objectId, String name, JsonObject address,
                                           JsonObject properties) {
        return new RemoteDeviceMixin(objectId, objectId.getInstanceNumber(), name,
                                     Optional.ofNullable(address).map(AddressMixin::create).orElse(null),
                                     PropertyValuesMixin.create(properties));
    }

    @Override
    public JsonObject toJson() {
        final JsonObject entries = getMapper().convertValue(this, JsonObject.class);
        if (Objects.isNull(propertyValues)) {
            return entries;
        }
        return entries.mergeIn(propertyValues.toJson());
    }

}
