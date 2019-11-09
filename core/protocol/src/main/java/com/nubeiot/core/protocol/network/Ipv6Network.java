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

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv6Network extends IpNetwork<Ipv6Network> implements Ethernet {

    private static final Predicate<InetAddress> IS_NAT_V6 = address -> !address.isAnyLocalAddress() &&
                                                                       !address.isMulticastAddress() &&
                                                                       !address.isLoopbackAddress() &&
                                                                       address instanceof Inet6Address;
    private static final Predicate<InterfaceAddress> IS_V6 = address -> IS_NAT_V6.test(address.getAddress());

    private Ipv6Network(Integer ifIndex, String ifName, String displayName, String macAddress, String cidrAddress,
                        String hostAddress) {
        super(ifIndex, ifName, displayName, macAddress, cidrAddress, hostAddress);
    }

    public static Ipv6Network from(NetworkInterface ni, InterfaceAddress ia) {
        return new Ipv6Network(ni.getIndex(), ni.getName(), ni.getDisplayName(), mac(ni),
                               cidr(ia.getAddress(), ia.getNetworkPrefixLength()), ia.getAddress().getHostAddress());
    }

    //TODO implement it
    private static String cidr(InetAddress address, short prefixLength) {
        return null;
    }

    public static List<Ipv6Network> getActiveIps() {
        return getActiveInterfaces(networkInterface -> true, IS_V6, Ipv6Network::from);
    }

    public static Ipv6Network getActiveIpByName(String interfaceName) {
        return getActiveIpByName(interfaceName, IS_V6, Ipv6Network::from);
    }

    public static Ipv6Network getFirstActiveIp() {
        return getFirstActiveIp(IS_V6, Ipv6Network::from);
    }

    @Override
    int version() {
        return 6;
    }

    @Override
    public Ipv6Network isReachable() throws CommunicationProtocolException {
        return this;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends EthernetBuilder<Ipv6Network, Builder> {

        @Override
        public Ipv6Network build() {
            return new Ipv6Network(ifIndex(), ifName(), displayName(), macAddress(), cidrAddress(), hostAddress());
        }

    }

}
