package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;

import com.nubeiot.core.enums.State;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.iotdata.edge.model.tables.pojos.Network;
import com.nubeiot.iotdata.translator.IoTEntityTranslator;

public final class BACnetNetworkTranslator
    implements BACnetTranslator<Network, CommunicationProtocol>, IoTEntityTranslator<Network, CommunicationProtocol> {

    @Override
    public Network serialize(CommunicationProtocol object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Invalid protocol");
        }
        return new Network().setProtocol(protocol())
                            .setCode(object.identifier())
                            .setState(State.ENABLED)
                            .setMetadata(object.toJson());
    }

    @Override
    public CommunicationProtocol deserialize(Network concept) {
        if (Objects.isNull(concept) || !protocol().equals(concept.getProtocol())) {
            throw new IllegalArgumentException("Invalid network");
        }
        return CommunicationProtocol.parse(concept.getMetadata().getMap());
    }

    @Override
    public Class<Network> fromType() {
        return Network.class;
    }

    @Override
    public Class<CommunicationProtocol> toType() {
        return CommunicationProtocol.class;
    }

}
