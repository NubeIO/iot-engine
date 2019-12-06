package com.nubeiot.edge.connector.bacnet.service.rpc;

import io.vertx.core.Vertx;

import com.nubeiot.core.component.SharedDataDelegate.AbstractSharedDataDelegate;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcClient;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolNetworkScanner;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

import lombok.NonNull;

/**
 * Represents a service that scans network in {@code Data Point repository} when startup {@code BACnet application}
 */
public final class BACnetNetworkScanner extends AbstractSharedDataDelegate<BACnetNetworkScanner>
    implements DataProtocolNetworkScanner<BACnetNetworkScanner>, BACnetRpcClient<BACnetNetworkScanner> {

    private final BACnetNetworkTranslator translator;

    BACnetNetworkScanner(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx);
        this.registerSharedKey(sharedKey);
        this.translator = new BACnetNetworkTranslator();
    }

    @Override
    public @NonNull IoTEntityTranslator<Network, CommunicationProtocol> translator() {
        return translator;
    }

}
