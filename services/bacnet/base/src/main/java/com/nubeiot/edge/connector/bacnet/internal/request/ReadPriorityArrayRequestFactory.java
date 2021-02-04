package com.nubeiot.edge.connector.bacnet.internal.request;

import io.github.zero88.qwe.dto.msg.RequestData;

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

}
