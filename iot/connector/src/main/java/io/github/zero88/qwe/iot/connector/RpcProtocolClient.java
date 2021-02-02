package io.github.zero88.qwe.iot.connector;

import io.github.zero88.qwe.iot.data.IoTEntity;
import io.github.zero88.qwe.protocol.Protocol;
import io.github.zero88.qwe.rpc.GatewayServiceInvoker;

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

    @Override
    @NonNull
    default String requester() {
        return protocol().type();
    }

    @Override
    default String serviceLabel() {
        return "RPC " + protocol().type() + " client";
    }

}
