package com.nubeiot.edge.connector.bacnet.dto;

import com.nubeiot.core.protocol.network.UdpProtocol;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class TransportIP implements TransportProvider {

    @NonNull
    private final UdpProtocol protocol;

    static TransportIP byConfig(@NonNull BACnetIP config) {
        return new TransportIP(config.toProtocol());
    }

    @Override
    public UdpProtocol protocol() {
        return protocol;
    }

    @Override
    public Transport get() {
        protocol.isReachable();
        final IpNetwork network = new IpNetworkBuilder().withPort(protocol.getPort())
                                                        .withReuseAddress(true)
                                                        .withSubnet(protocol.getIp().getSubnetAddress(),
                                                                    protocol.getIp().getSubnetPrefixLength())
                                                        .build();
        return new DefaultTransport(network);
    }

}
