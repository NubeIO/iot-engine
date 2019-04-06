package com.nubeiot.edge.connector.bacnet;

import java.net.InterfaceAddress;

import com.nubeiot.core.utils.Networks;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class TransportIP implements TransportProvider {

    private final Transport transport;

    static TransportIP autoSelect() {
        return create(Networks.firstNATIPv4());
    }

    static TransportIP bySubnet(String subnet) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static TransportIP byName(String networkInterface) {
        return create(Networks.findByName(networkInterface));
    }

    private static TransportIP create(@NonNull InterfaceAddress networkInterface) {
        IpNetwork network = new IpNetworkBuilder().withBroadcast(networkInterface.getBroadcast().getHostAddress(),
                                                                 networkInterface.getNetworkPrefixLength()).build();
        return new TransportIP(new DefaultTransport(network));
    }

    @Override
    public Transport get() {
        return transport;
    }

}
