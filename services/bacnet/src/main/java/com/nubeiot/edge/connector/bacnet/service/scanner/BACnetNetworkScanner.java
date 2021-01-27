package com.nubeiot.edge.connector.bacnet.service.scanner;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.protocol.CommunicationProtocol;

import com.nubeiot.core.rpc.scanner.AbstractRpcScanner;
import com.nubeiot.core.rpc.scanner.NetworkRpcScanner;
import com.nubeiot.edge.connector.bacnet.converter.BACnetNetworkConverter;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcProtocol;

import lombok.NonNull;

/**
 * Represents a service that scans network in {@code Data Point repository} when startup {@code BACnet application}
 *
 * @since 1.0.0
 */
public final class BACnetNetworkScanner extends AbstractRpcScanner<BACnetNetwork, CommunicationProtocol>
    implements NetworkRpcScanner<BACnetNetwork>, BACnetRpcProtocol<BACnetNetwork> {

    BACnetNetworkScanner(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData, new BACnetNetworkConverter());
    }

    @Override
    public @NonNull Class<BACnetNetwork> context() {
        return BACnetNetwork.class;
    }

}
