package com.nubeiot.edge.connector.bacnet.converter;

import java.util.Objects;

import io.github.zero88.qwe.protocol.CommunicationProtocol;

import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;
import com.nubeiot.edge.connector.bacnet.entity.BACnetProtocol;
import com.nubeiot.iotdata.converter.IoTEntityConverter;

public final class BACnetNetworkConverter
    implements BACnetProtocol, IoTEntityConverter<BACnetNetwork, CommunicationProtocol> {

    @Override
    public BACnetNetwork serialize(CommunicationProtocol object) {
        if (Objects.isNull(object)) {
            throw new IllegalArgumentException("Invalid protocol");
        }
        return BACnetNetwork.factory(object.toJson());
    }

    @Override
    public CommunicationProtocol deserialize(BACnetNetwork concept) {
        //        if (Objects.isNull(concept) || !protocol().equals(concept.getProtocol())) {
        //            throw new IllegalArgumentException("Invalid network");
        //        }
        //        final JsonObject metadata = Optional.ofNullable(concept.getMetadata()).orElse(new JsonObject());
        //        final String type = metadata.getString("type");
        //        if (Strings.isBlank(type)) {
        //            return CommunicationProtocol.parse(concept.getCode());
        //        }
        return concept.toProtocol();
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
