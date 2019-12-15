package com.nubeiot.edge.connector.bacnet.discover;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.dto.LocalDeviceMetadata;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

@Getter
@FieldNameConstants
@Builder(builderClassName = "Builder")
public final class DiscoverResponse implements JsonData {

    private CommunicationProtocol network;
    private LocalDeviceMetadata localDevice;
    private JsonArray remoteDevices;
    private JsonObject remoteDevice;
    private JsonArray objects;
    private JsonObject object;


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {}

}
