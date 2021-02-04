package com.nubeiot.edge.connector.bacnet.service;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.BaseProtocol;
import io.github.zero88.qwe.iot.connector.rpc.RpcClient;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;

import lombok.NonNull;

public abstract class AbstractBACnetRpcClient extends BaseProtocol implements RpcClient, BACnetProtocol {

    protected AbstractBACnetRpcClient(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull String gatewayAddress() {
        return sharedData().getData(BACnetCacheInitializer.GATEWAY_ADDRESS);
    }

}
