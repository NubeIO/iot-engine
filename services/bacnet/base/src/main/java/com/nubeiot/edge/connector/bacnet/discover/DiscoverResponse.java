package com.nubeiot.edge.connector.bacnet.discover;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.mixin.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.mixin.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.mixin.RemoteDeviceMixin;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@FieldNameConstants
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = DiscoverResponse.Builder.class)
public final class DiscoverResponse implements JsonData {

    private CommunicationProtocol network;
    private LocalDeviceMetadata localDevice;
    private List<RemoteDeviceMixin> remoteDevices;
    private RemoteDeviceMixin remoteDevice;
    private ObjectPropertyValues objects;
    private PropertyValuesMixin object;

    @Override
    public JsonObject toJson() {
        final JsonObject json = JsonData.super.toJson();
        Optional.ofNullable(object).ifPresent(o -> json.put(Fields.object, object.toJson()));
        Optional.ofNullable(objects).ifPresent(o -> json.put(Fields.objects, objects.toJson()));
        Optional.ofNullable(remoteDevice).ifPresent(o -> json.put(Fields.remoteDevice, remoteDevice.toJson()));
        Optional.ofNullable(remoteDevices)
                .ifPresent(o -> json.put(Fields.remoteDevices, remoteDevices.stream()
                                                                            .map(RemoteDeviceMixin::toJson)
                                                                            .collect(Collectors.toList())));
        return json;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
