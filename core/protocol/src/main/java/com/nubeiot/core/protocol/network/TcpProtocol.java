package com.nubeiot.core.protocol.network;

import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.NonNull;

public interface TcpProtocol extends TransportProtocol {

    @Override
    default @NonNull String type() {
        return "tcp" + getIp().version();
    }

    @Override
    @NonNull TcpProtocol isReachable() throws CommunicationProtocolException;

}
