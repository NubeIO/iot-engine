package com.nubeiot.edge.connector.bacnet.entity;

import java.util.Objects;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.edge.connector.bacnet.converter.property.DeviceStatusConverter;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectIdentifierMixin;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;
import com.nubeiot.iotdata.entity.AbstractDevice;
import com.nubeiot.iotdata.enums.DeviceStatus;
import com.nubeiot.iotdata.enums.DeviceType;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@SuperBuilder
@Accessors(fluent = true)
public class BACnetDeviceEntity extends AbstractDevice<String> implements BACnetEntity<String> {

    @NonNull
    @JsonIgnore
    private final RemoteDeviceMixin mixin;

    public static BACnetDeviceEntity from(@NonNull String networkId, @NonNull RemoteDeviceMixin mixin) {
        final PropertyValuesMixin values = mixin.getPropertyValues();
        final DeviceType type = Objects.nonNull(values.encode(PropertyIdentifier.deviceAddressBinding))
                                ? DeviceType.GATEWAY
                                : DeviceType.factory(values.encode(PropertyIdentifier.deviceType));
        final DeviceStatus status = new DeviceStatusConverter().serialize(
            (com.serotonin.bacnet4j.type.enumerated.DeviceStatus) values.getAndCast(PropertyIdentifier.systemStatus)
                                                                        .orElse(
                                                                            com.serotonin.bacnet4j.type.enumerated.DeviceStatus.nonOperational));
        return BACnetDeviceEntity.builder()
                                 .networkId(networkId)
                                 .key(ObjectIdentifierMixin.serialize(mixin.getObjectId()))
                                 .type(type)
                                 .name(mixin.getName())
                                 .status(status)
                                 .mixin(mixin)
                                 .build();
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = super.toJson();
        return json.mergeIn(mixin.toJson());
    }

}
