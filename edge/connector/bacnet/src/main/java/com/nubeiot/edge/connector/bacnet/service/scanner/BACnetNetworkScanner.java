package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.vertx.core.Vertx;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.rpc.query.AbstractDataProtocolScanner;
import com.nubeiot.edge.module.datapoint.rpc.query.DataProtocolNetworkScanner;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;

import lombok.NonNull;

/**
 * Represents a service that scans network in {@code Data Point repository} when startup {@code BACnet application}
 *
 * @since 1.0.0
 */
public final class BACnetNetworkScanner
    extends AbstractDataProtocolScanner<Network, CommunicationProtocol, BACnetNetworkScanner>
    implements DataProtocolNetworkScanner<BACnetNetworkScanner>, BACnetRpcProtocol {

    BACnetNetworkScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey, new BACnetNetworkTranslator());
    }

}
