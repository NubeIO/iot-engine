package com.nubeiot.edge.connector.bacnet.entity;

import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.github.zero88.utils.Strings;
import io.vertx.core.json.JsonObject;

import com.nubeiot.iotdata.entity.HasObjectType;
import com.nubeiot.iotdata.entity.INetwork;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class BACnetNetwork implements HasObjectType<String>, BACnetEntity<String>, INetwork<String> {

    private final String type;
    private final String label;

    BACnetNetwork(String type, String label) {
        this.type = type;
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

    @Override
    public @NonNull String key() {
        return toProtocol().identifier();
    }

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
