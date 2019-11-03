package com.nubeiot.edge.connector.bacnet.dto;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NetworkException;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.UdpProtocol;
import com.nubeiot.core.utils.Functions;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.util.sero.IpAddressUtils;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class TransportIP implements TransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportIP.class);
    @NonNull
    private final UdpProtocol protocol;

    static TransportIP byConfig(@NonNull BACnetIP config) {
        return new TransportIP(config.toProtocol());
    }

    static IpNetwork bySubnet(String subnet, int port) {
        String[] parts = Strings.requireNotBlank(subnet).split("/", 2);
        int prefix = Ipv4Network.parsePrefixLength(Functions.getIfThrow(() -> parts[1]).orElse(""));
        String check = IpAddressUtils.checkIpMask(parts[0]);
        if (Strings.isNotBlank(check)) {
            throw new NetworkException("Subnet is invalid: " + check);
        }
        final IpNetworkBuilder builder = new IpNetworkBuilder().withSubnet(parts[0], prefix);
        return builder.withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT)).withReuseAddress(true).build();
    }

    static IpNetwork byNetworkName(String networkName, int port) {
        Ipv4Network network = Strings.isBlank(networkName)
                              ? Ipv4Network.getFirstActiveIp()
                              : Ipv4Network.getActiveIpByName(networkName);
        LOGGER.info("Interface address for BACnetIP: {}", network.identifier());
        return new IpNetworkBuilder().withSubnet(network.getBroadcastAddress(), network.getPrefixLength())
                                     .withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT))
                                     .withReuseAddress(true)
                                     .build();
    }

    @Override
    public UdpProtocol protocol() {
        return protocol;
    }

    @Override
    public Transport get() {
        final UdpProtocol reachable = protocol.isReachable();
        final IpNetwork network = Strings.isNotBlank(reachable.getCidrAddress())
                                  ? bySubnet(reachable.getCidrAddress(), reachable.getPort())
                                  : byNetworkName(reachable.getIfName(), reachable.getPort());
        return new DefaultTransport(network);
    }

}
