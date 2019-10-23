package com.nubeiot.edge.connector.bacnet.dto;

import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.EnumType.AbstractEnumType;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class BACnetNetwork extends AbstractEnumType {

    @NonNull
    private final String name;
    private final int port;

    BACnetNetwork(String type, String name, int port) {
        super(type);
        this.name = Strings.requireNotBlank(name);
        this.port = Networks.validPort(port, IpNetwork.DEFAULT_PORT);
    }

    public static BACnetNetwork factory(@NonNull JsonObject data) {
        String type = (String) data.remove("type");
        if (BACnetIP.TYPE.equals(type)) {
            return JsonData.convert(data, BACnetIP.class);
        }
        if (BACnetMSTP.TYPE.equals(type)) {
            return JsonData.convert(data, BACnetMSTP.class);
        }
        throw new IllegalArgumentException(
            "Not support BACnet network type " + type + ". Only BACnet " + BACnetIP.TYPE + " or BACnet " +
            BACnetMSTP.TYPE);
    }

    @SuppressWarnings("unchecked")
    static abstract class BACnetNetworkBuilder<T extends BACnetNetwork, B extends BACnetNetworkBuilder> {

        String name;
        int port;

        public B port(int port) {
            this.port = port;
            return (B) this;
        }

        public B name(String name) {
            this.name = name;
            return (B) this;
        }

        public abstract T build();

    }

}
