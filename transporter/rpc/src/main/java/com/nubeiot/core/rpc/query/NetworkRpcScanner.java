package com.nubeiot.core.rpc.query;

import com.nubeiot.iotdata.entity.INetwork;

/**
 * Represents a service that scans {@code network} in {@code Data Point repository} when startup the specified {@code
 * protocol application}.
 *
 * @param <T> Type of {@code DataProtocolNetworkScanner}
 * @since 1.0.0
 */
public interface NetworkRpcScanner<N extends INetwork, X, T extends NetworkRpcScanner>
    /*extends RpcScanner<N, CommunicationProtocol, T>*/ {

    //    @Override
    //    default @NonNull Class<N> context() {
    //        return NetworkMetadata.INSTANCE;
    //    }
    //
    //    @Override
    //    default @NonNull String parseKey(@NonNull Network entity) {
    //        return Strings.toString(entity.getId());
    //    }
    //
    //    /**
    //     * Do scan network in repository
    //     *
    //     * @return map of network data id and communication protocol
    //     * @see CommunicationProtocol
    //     * @since 1.0.0
    //     */
    //    default @NonNull Single<Map<String, CommunicationProtocol>> scan() {
    //        final Network network = new Network().setProtocol(protocol()).setState(State.ENABLED);
    //        return query(RequestData.builder().body(JsonPojo.from(network).toJson()).build());
    //    }
}
