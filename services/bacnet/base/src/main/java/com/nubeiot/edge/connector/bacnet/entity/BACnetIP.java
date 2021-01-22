package com.nubeiot.edge.connector.bacnet.entity;

import io.github.zero88.qwe.utils.Networks;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BACnetIP.Builder.class)
public final class BACnetIP extends BACnetNetwork {

    public static final String TYPE = "IP";
    private final String subnet;
    private final String networkInterface;
    private final int port;

    private BACnetIP(String label, int port, String subnet, String networkInterface) {
        super(TYPE, label);
        this.subnet = subnet;
        this.networkInterface = networkInterface;
        this.port = Networks.validPort(port, IpNetwork.DEFAULT_PORT);
    }

    @Override
    public @NonNull UdpProtocol toProtocol() {
        return UdpProtocol.builder()
                          .port(this.port)
                          .ifName(this.networkInterface)
                          .cidrAddress(this.subnet)
                          .displayName(label())
                          .canReusePort(true)
                          .build();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends BACnetNetworkBuilder<BACnetIP, BACnetIP.Builder> {

        public BACnetIP build() {
            return new BACnetIP(label, port, subnet, networkInterface);
        }

    }

}
