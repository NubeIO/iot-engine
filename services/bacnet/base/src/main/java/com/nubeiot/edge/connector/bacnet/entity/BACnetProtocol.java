package com.nubeiot.edge.connector.bacnet.entity;

import com.nubeiot.iotdata.HasProtocol;
import com.nubeiot.iotdata.Protocol;

import lombok.NonNull;

public interface BACnetProtocol extends HasProtocol {

    @Override
    default @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

}
