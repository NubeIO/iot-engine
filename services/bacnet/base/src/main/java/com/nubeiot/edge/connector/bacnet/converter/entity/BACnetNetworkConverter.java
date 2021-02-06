package com.nubeiot.edge.connector.bacnet.converter.entity;

import java.util.Optional;

import io.github.zero88.qwe.iot.data.converter.IoTEntityConverter;
import io.github.zero88.qwe.protocol.CommunicationProtocol;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;

public final class BACnetNetworkConverter
    implements BACnetProtocol, IoTEntityConverter<BACnetNetwork, CommunicationProtocol> {

    @Override
    public BACnetNetwork serialize(CommunicationProtocol protocol) {
        return Optional.ofNullable(protocol).map(BACnetNetwork::fromProtocol).orElse(null);
    }

    @Override
    public CommunicationProtocol deserialize(BACnetNetwork concept) {
        return Optional.ofNullable(concept).map(BACnetNetwork::toProtocol).orElse(null);
    }

    @Override
    public Class<BACnetNetwork> fromType() {
        return BACnetNetwork.class;
    }

    @Override
    public Class<CommunicationProtocol> toType() {
        return CommunicationProtocol.class;
    }

}
