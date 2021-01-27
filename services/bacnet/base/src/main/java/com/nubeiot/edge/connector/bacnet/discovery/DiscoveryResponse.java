package com.nubeiot.edge.connector.bacnet.discovery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@FieldNameConstants
@Builder(builderClassName = "Builder")
public final class DiscoveryResponse implements JsonData {

    private final CommunicationProtocol network;
    private final LocalDeviceMetadata localDevice;
    private final List<RemoteDeviceMixin> remoteDevices;
    private final RemoteDeviceMixin remoteDevice;
    private final ObjectPropertyValues objects;
    private final PropertyValuesMixin object;

    @Override
    public JsonObject toJson() {
        final JsonObject json = JsonData.super.toJson();
        Optional.ofNullable(object).ifPresent(o -> json.put(Fields.object, object.toJson()));
        Optional.ofNullable(objects).ifPresent(o -> json.put(Fields.objects, objects.toJson()));
        Optional.ofNullable(remoteDevice).ifPresent(o -> json.put(Fields.remoteDevice, remoteDevice.toJson()));
        Optional.ofNullable(remoteDevices)
                .ifPresent(o -> json.put(Fields.remoteDevices, remoteDevices.stream().map(RemoteDeviceMixin::toJson)
                                                                            .collect(Collectors.toList())));
        return json;
    }

}
