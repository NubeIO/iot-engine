package com.nubeiot.core.protocol.network;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(chain = true)
@Builder(builderClassName = "Builder")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonDeserialize(builder = UdpProtocol.Builder.class)
public final class UdpProtocol implements TransportProtocol {

    @NonNull
    private IpNetwork ip;
    private int port;

    @Override
    public @NonNull String type() {
        return "udp" + getIp().version();
    }

    @Override
    public @NonNull UdpProtocol isReachable() throws CommunicationProtocolException {
        return this;
    }

}
