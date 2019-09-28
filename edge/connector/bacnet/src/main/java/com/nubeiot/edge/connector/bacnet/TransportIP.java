package com.nubeiot.edge.connector.bacnet;

import java.net.InterfaceAddress;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NetworkException;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.dto.BACnetIP;
import com.nubeiot.edge.connector.bacnet.dto.BACnetNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.npdu.ip.IpNetworkBuilder;
import com.serotonin.bacnet4j.transport.DefaultTransport;
import com.serotonin.bacnet4j.transport.Transport;
import com.serotonin.bacnet4j.util.sero.IpAddressUtils;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class TransportIP implements TransportProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportIP.class);
    @NonNull
    private final BACnetNetwork config;
    @NonNull
    private final Transport transport;

    static TransportIP byConfig(@NonNull BACnetIP config) {
        IpNetwork network = Strings.isNotBlank(config.getSubnet())
                            ? bySubnet(config.getSubnet(), config.getPort())
                            : byNetworkName(config.getNetworkInterface(), config.getPort());
        return new TransportIP(config, new DefaultTransport(network));
    }

    static IpNetwork bySubnet(String subnet, int port) {
        String[] parts = Strings.requireNotBlank(subnet).split("/");
        int prefix = parts.length < 2 ? 0 : Strings.convertToInt(parts[1], 0);
        String check = IpAddressUtils.checkIpMask(parts[0]);
        if (Strings.isNotBlank(check)) {
            throw new NetworkException("Subnet is invalid: " + check);
        }
        return new IpNetworkBuilder().withSubnet(parts[0], prefix)
                                     .withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT))
                                     .build();
    }

    static IpNetwork byNetworkName(String networkName, int port) {
        InterfaceAddress address = Strings.isBlank(networkName)
                                   ? Networks.firstNATIPv4()
                                   : Networks.findByName(networkName);
        LOGGER.info("Interface address for BACnetIP: {}", address.toString());
        return new IpNetworkBuilder().withBroadcast(address.getBroadcast().getHostAddress(),
                                                    address.getNetworkPrefixLength())
                                     .withPort(Networks.validPort(port, IpNetwork.DEFAULT_PORT))
                                     .build();
    }

    @Override
    public Transport get() { return transport; }

    @Override
    public BACnetNetwork config() {
        return config;
    }

}
