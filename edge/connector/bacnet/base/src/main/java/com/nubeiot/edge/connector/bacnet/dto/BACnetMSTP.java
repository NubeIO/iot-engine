package com.nubeiot.edge.connector.bacnet.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BACnetMSTP.Builder.class)
public final class BACnetMSTP extends BACnetNetwork {

    public static final String TYPE = "MSTP";
    private final int baud;
    private final int parity;
    private final int buffer;

    private BACnetMSTP(String name, int port, int baud, int parity, int buffer) {
        super(TYPE, name, port);
        this.baud = baud;
        this.parity = parity;
        this.buffer = buffer;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends BACnetNetworkBuilder<BACnetMSTP, BACnetMSTP.Builder> {

        public BACnetMSTP build() {
            return new BACnetMSTP(name, port, baud, parity, buffer);
        }

    }

}
