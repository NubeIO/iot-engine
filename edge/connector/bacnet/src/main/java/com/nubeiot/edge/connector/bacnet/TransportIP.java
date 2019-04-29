package com.nubeiot.edge.connector.bacnet;

import java.net.InterfaceAddress;

import com.nubeiot.core.exceptions.NetworkException;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.BACnetConfig.IPConfig;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.util.sero.IpAddressUtils;

class TransportIP implements TransportProvider {

    private final Transport transport;

    private TransportIP(IpNetwork network) { this.transport = new DefaultTransport(network); }

    static TransportIP byConfig(IPConfig config) {
        if (Strings.isNotBlank(config.getSubnet())) {
            return bySubnet(config.getSubnet(), config.getPort());
        }
        return byNetworkName(config.getNetworkInterface(), config.getPort());
    }

    static TransportIP bySubnet(String subnet, int port) {
        String[] parts = Strings.requireNotBlank(subnet).split("/");
        int prefix = parts.length < 2 ? 0 : Strings.convertToInt(parts[1], 0);
        String check = IpAddressUtils.checkIpMask(parts[0]);
        if (Strings.isNotBlank(check)) {
            throw new NetworkException("Subnet is invalid: " + check);
        }
        IpNetworkBuilder builder = new IpNetworkBuilder().withSubnet(parts[0], prefix)
                                                         .withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT));
        return new TransportIP(builder.build());
    }

    static TransportIP byNetworkName(String networkName, int port) {
        InterfaceAddress address = Strings.isBlank(networkName)
                                   ? Networks.firstNATIPv4()
                                   : Networks.findByName(networkName);
        IpNetworkBuilder builder = new IpNetworkBuilder().withBroadcast(address.getBroadcast().getHostAddress(),
                                                                        address.getNetworkPrefixLength())
                                                         .withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT));
        return new TransportIP(builder.build());
    }

    @Override
    public Transport get() { return transport; }

}
