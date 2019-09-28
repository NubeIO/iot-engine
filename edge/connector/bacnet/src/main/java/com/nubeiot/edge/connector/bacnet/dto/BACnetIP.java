package com.nubeiot.edge.connector.bacnet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BACnetIP.Builder.class)
public final class BACnetIP extends BACnetNetwork {

    public static final String TYPE = "IP";
    private final String subnet;
    private final String networkInterface;

    private BACnetIP(String name, int port, String subnet, String networkInterface) {
        super(TYPE, name, port);
        this.subnet = subnet;
        this.networkInterface = networkInterface;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends BACnetNetworkBuilder<BACnetIP, BACnetIP.Builder> {

        public BACnetIP build() {
            return new BACnetIP(name, port, subnet, networkInterface);
        }

    }

}
