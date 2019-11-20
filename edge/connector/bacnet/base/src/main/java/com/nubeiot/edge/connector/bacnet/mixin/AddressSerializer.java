package com.nubeiot.edge.connector.bacnet.mixin;

import java.io.IOException;

import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.nubeiot.core.protocol.network.IpNetwork;
import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetMSTP;
import com.serotonin.bacnet4j.type.constructed.Address;
import com.serotonin.bacnet4j.type.primitive.OctetString;

import lombok.NonNull;

public final class AddressSerializer extends EncodableSerializer<Address> {

    AddressSerializer() {
        super(Address.class);
    }

    public static JsonObject serialize(@NonNull Address value) {
        final OctetString mac = value.getMacAddress();
        return new JsonObject().put("type", mac.getLength() == 1 ? BACnetMSTP.TYPE : BACnetIP.TYPE)
                               .put("networkNumber", value.getNetworkNumber().intValue())
                               .put("hostAddress", mac.getDescription())
                               .put("macAddress", IpNetwork.mac(mac.getBytes()));
    }

    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(serialize(value));
    }

}
