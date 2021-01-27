package com.nubeiot.core.rpc;

import io.github.zero88.qwe.protocol.Protocol;
import io.github.zero88.qwe.rpc.GatewayServiceInvoker;

import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

/**
 * Represents {@code RPC client} that supports remote call to {@code external services} via service Locator
 *
 * @param <P> Type of IoT entity
 * @see GatewayServiceInvoker
 * @see RpcProtocol
 * @see IoTEntity
 * @see Protocol
 */
public interface RpcProtocolClient<P extends IoTEntity> extends GatewayServiceInvoker, RpcProtocol<P> {

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
        return "RPC protocol client";
    }

}
