package com.nubeiot.core.rpc;

import io.github.zero88.qwe.component.SharedDataDelegate;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.micro.discovery.GatewayServiceInvoker;

import com.nubeiot.iotdata.IoTEntity;
import com.nubeiot.iotdata.Protocol;

import lombok.NonNull;

/**
 * Represents {@code data-point RPC client} that supports remote call to {@code data-point services}
 *
 * @param <T> Type of RPC client
 * @see GatewayServiceInvoker
 * @see SharedDataDelegate
 * @see Protocol
 */
public interface RpcClient<P extends IoTEntity, T extends RpcClient>
    extends GatewayServiceInvoker, RpcProtocol<P>, SharedDataDelegate<T> {

    String GATEWAY_ADDRESS = "GATEWAY_ADDRESS";

    @Override
    @NonNull
    default String gatewayAddress() {
        return getSharedDataValue(GATEWAY_ADDRESS);
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
        return "Edge data-point service";
    }

    @Override
    default EventbusClient transporter() {
        return getSharedDataValue(SHARED_EVENTBUS);
    }

}
