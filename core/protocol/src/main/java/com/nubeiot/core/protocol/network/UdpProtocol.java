package com.nubeiot.core.protocol.network;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = UdpProtocol.Builder.class)
public final class UdpProtocol extends TransportProtocol {

    private UdpProtocol(IpNetwork ip, int port) {
        super(ip, port);
    }

    @Override
    public @NonNull String type() {
        return "udp" + getIp().version();
    }

    @Override
    public @NonNull UdpProtocol isReachable() throws CommunicationProtocolException {
        return this;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder extends TransportBuilder<UdpProtocol, Builder> {

        @Override
        public UdpProtocol build() {
            return new UdpProtocol(Optional.ofNullable(ip()).orElseGet(this::buildIp), port());
        }

    }

}
