package com.nubeiot.edge.connector.bacnet.discover;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;
import com.nubeiot.edge.connector.bacnet.dto.ObjectPropertyValues;
import com.nubeiot.edge.connector.bacnet.dto.PropertyValuesMixin;
import com.nubeiot.edge.connector.bacnet.dto.RemoteDeviceMixin;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@FieldNameConstants
@Builder(builderClassName = "Builder")
public final class DiscoverResponse implements JsonData {

    private CommunicationProtocol network;
    private LocalDeviceMetadata localDevice;
    private List<RemoteDeviceMixin> remoteDevices;
    private RemoteDeviceMixin remoteDevice;
    private ObjectPropertyValues objects;
    private PropertyValuesMixin object;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
