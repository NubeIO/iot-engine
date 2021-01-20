package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.rpc.query.AbstractRpcScanner;
import com.nubeiot.core.rpc.query.NetworkRpcScanner;
import com.nubeiot.edge.connector.bacnet.converter.BACnetNetworkConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;

import lombok.NonNull;

/**
 * Represents a service that scans network in {@code Data Point repository} when startup {@code BACnet application}
 *
 * @since 1.0.0
 */
public final class BACnetNetworkRpcScanner
    extends AbstractRpcScanner<BACnetNetwork, CommunicationProtocol, BACnetNetworkRpcScanner>
    implements NetworkRpcScanner<BACnetNetwork, CommunicationProtocol, BACnetNetworkRpcScanner>,
               BACnetRpcProtocol<BACnetNetwork> {

    BACnetNetworkRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetNetworkConverter());
    }

    @Override
    public @NonNull Class<BACnetNetwork> context() {
        return BACnetNetwork.class;
    }

}
