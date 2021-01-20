package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.rpc.query.NetworkRpcScanner;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;

import lombok.NonNull;

/**
 * Represents a service that scans network in {@code Data Point repository} when startup {@code BACnet application}
 *
 * @since 1.0.0
 */
public final class BACnetNetworkRpcScanner
    extends AbstractDataProtocolScanner<Network, CommunicationProtocol, BACnetNetworkRpcScanner>
    implements NetworkRpcScanner<BACnetNetworkRpcScanner>, BACnetRpcProtocol {

    BACnetNetworkRpcScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetNetworkTranslator());
    }

}
