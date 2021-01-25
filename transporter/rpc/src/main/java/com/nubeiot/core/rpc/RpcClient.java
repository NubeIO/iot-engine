package com.nubeiot.core.rpc;

import io.github.zero88.qwe.micro.discovery.GatewayServiceInvoker;

import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents {@code RPC client} that supports remote call to {@code external services} via service Locator
 *
 * @param <P> Type of IoT entity
 * @see GatewayServiceInvoker
 * @see IoTEntity
 */
public interface RpcClient<P extends IoTEntity> extends GatewayServiceInvoker, RpcProtocol<P> {

    String GATEWAY_ADDRESS = "GATEWAY_ADDRESS";

    @Override
    @NonNull
    default String gatewayAddress() {
        return sharedData().getData(GATEWAY_ADDRESS);
    }

    @Override
    default @NonNull String destination() { return ""; }

    @Override
    @NonNull
    default String requester() {
        return protocol().type();
    }

    @Override
    default String serviceLabel() {
        return "RPC client";
    }

}
