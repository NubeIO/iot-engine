package com.nubeiot.edge.connector.bacnet.dto;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BACnetNetwork extends AbstractEnumType {

    private final String label;

    BACnetNetwork(String type, String label) {
        super(type);
        this.label = label;
    }

    public static BACnetNetwork factory(@NonNull JsonObject data) {
        String type = (String) data.remove("type");
        if (Strings.isBlank(type) || BACnetIP.TYPE.equals(type)) {
            return JsonData.convert(data, BACnetIP.class);
        }
        if (BACnetMSTP.TYPE.equals(type)) {
            return JsonData.convert(data, BACnetMSTP.class);
        }
        throw new IllegalArgumentException(
            "Not support BACnet network type " + type + ". Only BACnet " + BACnetIP.TYPE + " or BACnet " +
            BACnetMSTP.TYPE);
    }

    public abstract @NonNull CommunicationProtocol toProtocol();

    @SuppressWarnings("unchecked")
    static abstract class BACnetNetworkBuilder<T extends BACnetNetwork, B extends BACnetNetworkBuilder> {

        String label;

        public B label(String label) {
            this.label = label;
            return (B) this;
        }

        public abstract T build();

    }

}
