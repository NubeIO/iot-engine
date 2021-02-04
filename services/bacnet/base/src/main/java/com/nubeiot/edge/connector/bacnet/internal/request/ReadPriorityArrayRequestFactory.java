package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.event.EventMessage;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.serotonin.bacnet4j.service.confirmed.ReadPropertyRequest;
import com.serotonin.bacnet4j.type.enumerated.PropertyIdentifier;

import lombok.NonNull;

public class ReadPriorityArrayRequestFactory implements ConfirmedRequestFactory<ReadPropertyRequest, RequestData> {

    @Override
    public @NonNull RequestData convertData(@NonNull DiscoveryArguments args, @NonNull RequestData requestData) {
        return requestData;
    }

    @Override
    public @NonNull ReadPropertyRequest factory(@NonNull DiscoveryArguments args, @NonNull RequestData data) {
        return new ReadPropertyRequest(args.params().objectCode(), PropertyIdentifier.priorityArray);
    }

    @Override
    public void then(@NonNull BACnetDevice device, @NonNull EventMessage result, @NonNull RequestData data,
                     @NonNull DiscoveryArguments args, @NonNull RequestData requestData) {

    }

}
