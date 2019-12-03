package com.nubeiot.edge.module.datapoint.rpc;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.discovery.RemoteServiceInvoker;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.iotdata.dto.Protocol;

import lombok.NonNull;

/**
 * Represents {@code data-point RPC client} that supports remote call to {@code data-point services}
 *
 * @param <T> Type of RPC client
 * @see RemoteServiceInvoker
 * @see SharedDataDelegate
 * @see Protocol
 */
public interface DataPointRpcClient<T extends DataPointRpcClient>
    extends RemoteServiceInvoker, RpcProtocol, SharedDataDelegate<T> {

    String GATEWAY_ADDRESS = "GATEWAY_ADDRESS";

    @Override
    default @NonNull String gatewayAddress() {
        return getSharedDataValue(GATEWAY_ADDRESS);
    }

    @Override
    default String requester() {
        return "service/" + protocol().type();
    }

    @Override
    default EventbusClient eventClient() {
        return getSharedDataValue(SHARED_EVENTBUS);
    }

    @Override
    default String serviceLabel() {
        return "Edge data-point service";
    }

    @Override
    default @NonNull String destination() {
        return DataPointIndex.lookupApiName(representation());
    }

    /**
     * Declares discovery service represents for what entity model
     *
     * @return entity metadata
     * @see EntityMetadata
     */
    @NonNull EntityMetadata representation();

}
