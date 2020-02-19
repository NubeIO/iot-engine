package com.nubeiot.edge.module.datapoint.rpc;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.discovery.GatewayServiceInvoker;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents {@code data-point RPC client} that supports remote call to {@code data-point services}
 *
 * @param <T> Type of RPC client
 * @see GatewayServiceInvoker
 * @see SharedDataDelegate
 * @see Protocol
 */
public interface DataProtocolRpcClient<T extends DataProtocolRpcClient>
    extends GatewayServiceInvoker, RpcProtocol, SharedDataDelegate<T> {

    String GATEWAY_ADDRESS = "GATEWAY_ADDRESS";

    @Override
    @NonNull
    default String gatewayAddress() {
        return getSharedDataValue(GATEWAY_ADDRESS);
    }

    @Override
    default @NonNull String destination() {
        return DataPointIndex.lookupApiName(context());
    }

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
