package com.nubeiot.core.protocol.network;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.exceptions.NotFoundException;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv6Network extends IpNetwork implements Ethernet {

    private static final Predicate<InetAddress> IS_NAT_V6 = address -> !address.isAnyLocalAddress() &&
                                                                       !address.isMulticastAddress() &&
                                                                       !address.isLoopbackAddress() &&
                                                                       address instanceof Inet6Address;
    private static final Predicate<InterfaceAddress> IS_V6 = address -> IS_NAT_V6.test(address.getAddress());

    private Ipv6Network(Integer ifIndex, String ifName, String displayName, String macAddress, String cidrAddress,
                        String hostAddress, short prefixLength) {
        super(ifIndex, ifName, displayName, macAddress, cidrAddress, hostAddress, prefixLength);
    }

    public static Ipv6Network from(NetworkInterface ni, InterfaceAddress ia) {
        return new Ipv6Network(ni.getIndex(), ni.getName(), ni.getDisplayName(), mac(ni),
                               cidr(ia.getAddress(), ia.getNetworkPrefixLength()), ia.getAddress().getHostAddress(),
                               ia.getNetworkPrefixLength());
    }

    //TODO implement it
    private static String cidr(InetAddress address, short prefixLength) {
        return null;
    }

    public static List<IpNetwork> getActiveIps() {
        return getActiveInterfaces(networkInterface -> true, IS_V6, Ipv6Network::from);
    }

    public static Ipv6Network getActiveIpByName(String interfaceName) {
        return getActiveInterfaces(networkInterface -> networkInterface.getName().equalsIgnoreCase(interfaceName),
                                   IS_V6, Ipv6Network::from).stream()
                                                            .findFirst()
                                                            .map(Ipv6Network.class::cast)
                                                            .orElseThrow(() -> new NotFoundException(
                                                                "Not found active network interface with name " +
                                                                interfaceName));
    }

    @Override
    public Ipv6Network isReachable() throws CommunicationProtocolException {
        return this;
    }

    @Override
    int version() {
        return 6;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends EthernetBuilder<Ipv6Network, Builder> {

        @Override
        public Ipv6Network build() {
            return new Ipv6Network(ifIndex(), ifName(), displayName(), macAddress(), cidrAddress(), hostAddress(),
                                   prefixLength());
        }

    }

}
