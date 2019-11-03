package com.nubeiot.core.protocol.network;

import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = TcpProtocol.Builder.class)
public final class TcpProtocol extends TransportProtocol {

    private TcpProtocol(@NonNull IpNetwork ip, int port) {
        super(ip, port);
    }

    @Override
    public @NonNull String type() {
        return "tcp" + getIp().version();
    }

    @Override
    public @NonNull TcpProtocol isReachable() throws CommunicationProtocolException {
        return this;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder extends TransportBuilder<TcpProtocol, TcpProtocol.Builder> {

        @Override
        public TcpProtocol build() {
            return new TcpProtocol(Optional.ofNullable(ip()).orElseGet(this::buildIp), port());
        }

    }

}
