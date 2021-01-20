package com.nubeiot.core.rpc.query;

import java.util.Map;

import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.utils.Strings;
import io.reactivex.Single;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

/**
 * Represents a service that scans {@code network} in {@code Data Point repository} when startup the specified {@code
 * protocol application}.
 *
 * @param <T> Type of {@code DataProtocolNetworkScanner}
 * @since 1.0.0
 */
public interface NetworkRpcScanner<T extends NetworkRpcScanner> extends RpcScanner<Network, CommunicationProtocol, T> {

    @Override
    default @NonNull NetworkMetadata context() {
        return NetworkMetadata.INSTANCE;
    }

    @Override
    default @NonNull String parseKey(@NonNull Network entity) {
        return Strings.toString(entity.getId());
    }

    /**
     * Do scan network in repository
     *
     * @return map of network data id and communication protocol
     * @see CommunicationProtocol
     * @since 1.0.0
     */
    default @NonNull Single<Map<String, CommunicationProtocol>> scan() {
        final Network network = new Network().setProtocol(protocol()).setState(State.ENABLED);
        return query(RequestData.builder().body(JsonPojo.from(network).toJson()).build());
    }

}
