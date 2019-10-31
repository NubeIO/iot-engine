package com.nubeiot.core.protocol.network;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv6Network extends IpNetwork implements Ethernet {

    private Ipv6Network(int index, String name, String displayName, String macAddress, String cidrAddress,
                        String hostAddress) {
        super(index, name, displayName, macAddress, cidrAddress, hostAddress);
    }

    @Override
    public Ipv6Network isReachable() throws CommunicationProtocolException {
        return this;
    }

    @Override
    public String identifier() {
        return type() + SPLIT_CHAR;
    }

    @Override
    int version() {
        return 6;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends IpBuilder<Ipv6Network, Ipv6Network.Builder> {

        @Override
        public Ipv6Network build() {
            return new Ipv6Network(index(), name(), displayName(), macAddress(), cidrAddress(), hostAddress());
        }

    }

}
