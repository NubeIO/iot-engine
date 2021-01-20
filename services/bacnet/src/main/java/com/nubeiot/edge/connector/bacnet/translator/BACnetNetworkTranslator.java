package com.nubeiot.edge.connector.bacnet.translator;

import java.util.Objects;
import java.util.Optional;

import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

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
        final JsonObject metadata = Optional.ofNullable(concept.getMetadata()).orElse(new JsonObject());
        final String type = metadata.getString("type");
        if (Strings.isBlank(type)) {
            return CommunicationProtocol.parse(concept.getCode());
        }
        return CommunicationProtocol.parse(metadata.getMap());
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
