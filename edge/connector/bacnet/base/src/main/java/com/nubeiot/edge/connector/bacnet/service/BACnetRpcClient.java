package com.nubeiot.edge.connector.bacnet.service;

import com.nubeiot.edge.module.datapoint.rpc.DataPointRpcClient;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents for BACnet RPC client that interacts with {@code data-point services}
 */
public interface BACnetRpcClient<T extends BACnetRpcClient> extends DataPointRpcClient<T> {

    @Override
    default @NonNull Protocol protocol() {
        return Protocol.BACNET;
    }

}
