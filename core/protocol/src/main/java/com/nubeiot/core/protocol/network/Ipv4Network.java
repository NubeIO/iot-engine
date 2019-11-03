package com.nubeiot.core.protocol.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv4Network extends IpNetwork implements Ethernet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ipv4Network.class);
    private static final String IPV4_REGEX
        = "(([0-1]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}([0-1]?\\d{1,2}|2[0-4]\\d|25[0-5])";
    private final String broadcastAddress;

    private Ipv4Network(Integer ifIndex, String ifName, String displayName, String macAddress, String cidrAddress,
                        String hostAddress, String broadcastAddress, short prefixLength) {
        super(ifIndex, ifName, displayName, macAddress, cidrAddress, hostAddress, prefixLength);
        this.broadcastAddress = broadcastAddress;
    }

    public static boolean isValidIPv4(String ip) {
        if (Strings.isBlank(ip)) {
            throw new IllegalArgumentException("IP can't be blank");
        }
        return Pattern.compile(IPV4_REGEX).matcher(ip).matches();
    }

    public static int validatePrefixLength(int prefixLength) {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid IPv4 prefix length, only [0,32]");
        }
        return prefixLength;
    }

    public static int parsePrefixLength(String prefixLength) {
        return validatePrefixLength(Strings.convertToInt(prefixLength, 32));
    }

    public static Ipv4Network from(@NonNull NetworkInterface ni, @NonNull InterfaceAddress ia) {
        return new Ipv4Network(ni.getIndex(), ni.getName(), ni.getDisplayName(), mac(ni),
                               cidr(ia.getAddress(), ia.getNetworkPrefixLength()), ia.getAddress().getHostAddress(),
                               ia.getBroadcast().getHostAddress(), ia.getNetworkPrefixLength());
    }

    public static List<IpNetwork> getActiveIps() {
        return getActiveInterfaces(networkInterface -> true, Networks.IS_V4, Ipv4Network::from);
    }

    public static Ipv4Network getActiveIpByName(String interfaceName) {
        return getActiveInterfaces(ni -> ni.getName().equalsIgnoreCase(interfaceName), Networks.IS_V4,
                                   Ipv4Network::from).stream()
                                                     .findFirst()
                                                     .map(Ipv4Network.class::cast)
                                                     .orElseThrow(notFound(interfaceName));
    }

    public static Ipv4Network getFirstActiveIp() {
        return getActiveInterfaces(ni -> true, Networks.IS_V4, Ipv4Network::from).stream()
                                                                                 .findFirst()
                                                                                 .map(Ipv4Network.class::cast)
                                                                                 .orElseThrow(notFound(null));
    }

    public static Ipv4Network getActiveIpByBroadcast(@NonNull String broadcast) {
        return getActiveInterfaces(ni -> true,
                                   Networks.IS_V4.and(ia -> ia.getBroadcast().getHostAddress().equals(broadcast)),
                                   Ipv4Network::from).stream()
                                                     .findFirst()
                                                     .map(Ipv4Network.class::cast)
                                                     .orElseThrow(notFound(null));
    }

    private static Supplier<NotFoundException> notFound(String interfaceName) {
        return () -> new NotFoundException("Not found active Ipv4 network interface" +
                                           Optional.ofNullable(interfaceName).map(n -> " with name " + n).orElse(""));
    }

    private static String cidr(@NonNull InetAddress ia, short prefixLength) {
        return Arrays.stream(intToByteArray(byteArrayToInt(ia.getAddress()) & mask(prefixLength)))
                     .mapToObj(String::valueOf)
                     .collect(Collectors.joining(".")) + "/" + prefixLength;
    }

    private static int byteArrayToInt(byte[] ba) {
        return ba[3] & 0xFF | (ba[2] & 0xFF) << 8 | (ba[1] & 0xFF) << 16 | (ba[0] & 0xFF) << 24;
    }

    private static int[] intToByteArray(int a) {
        return new int[] {(a >> 24) & 0xFF, (a >> 16) & 0xFF, (a >> 8) & 0xFF, a & 0xFF};
    }

    @Override
    public Ipv4Network isReachable() throws CommunicationProtocolException {
        return this;
    }

    @Override
    int version() {
        return 4;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends EthernetBuilder<Ipv4Network, Builder> {

        @Override
        public Ipv4Network build() {
            return new Ipv4Network(ifIndex(), ifName(), displayName(), macAddress(), cidrAddress(), hostAddress(),
                                   broadcastAddress, prefixLength());
        }

    }

}
