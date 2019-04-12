package com.nubeiot.edge.connector.bacnet;

import java.net.InterfaceAddress;

import com.nubeiot.core.exceptions.NubeException;
import com.nubeiot.core.utils.Networks;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class TransportIP implements TransportProvider {

    private final IpNetwork network;

    //    static TransportIP autoSelect() {
    //        return create(Networks.firstNATIPv4());
    //    }
    //
    //    static TransportIP bySubnet(String subnet) {
    //        throw new UnsupportedOperationException("Not yet implemented");
    //    }
    //
    //    static TransportIP byName(String networkInterface) {
    //        return create(Networks.findByName(networkInterface));
    //    }

    static TransportIP byAll(String ip, String networkName, Integer port) {
        IpNetworkBuilder builder = new IpNetworkBuilder();
        InterfaceAddress address = null;
        if (ip != null && !ip.isEmpty()) {
            //            builder.withLocalBindAddress(ip);
            throw new UnsupportedOperationException("Not yet implemented");
        } else if (networkName != null && !networkName.isEmpty()) {
            address = Networks.findByName(networkName);
        } else {
            address = Networks.firstNATIPv4();
        }

        if (address == null) {
            throw new NubeException("No network interface found");
        }
        if (port != null && port != 0) {
            builder.withPort(port);
        }

        return new TransportIP(
            builder.withBroadcast(address.getBroadcast().getHostAddress(), address.getNetworkPrefixLength()).build());
    }

    @Override
    public DefaultTransport get() { return new DefaultTransport(network); }

}
