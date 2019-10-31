package com.nubeiot.core.protocol.network;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.NonNull;

public interface TransportProtocol extends Ethernet {

    @JsonUnwrapped
    @NonNull IpNetwork getIp();

    int getPort();

    @Override
    @NonNull TransportProtocol isReachable() throws CommunicationProtocolException;

    @Override
    default int getIndex() {
        return getIp().getIndex();
    }

    @Override
    default String getName() {
        return getIp().getName();
    }

    @Override
    default String getDisplayName() {
        return getIp().getDisplayName();
    }

    @Override
    default String getMacAddress() {
        return getIp().getMacAddress();
    }

    @Override
    default String getCidrAddress() {
        return getIp().getCidrAddress();
    }

    @Override
    default String getHostAddress() {
        return getIp().getHostAddress();
    }

    @Override
    default @NonNull String identifier() {
        return type() + SPLIT_CHAR + getIp().identifier() + SPLIT_CHAR + getPort();
    }

}
