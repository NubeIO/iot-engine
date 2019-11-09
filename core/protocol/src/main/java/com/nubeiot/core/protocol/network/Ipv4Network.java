package com.nubeiot.core.protocol.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv4Network extends IpNetwork<Ipv4Network> implements Ethernet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ipv4Network.class);
    private static final String IPV4_REGEX
        = "(([0-1]?\\d{1,2}|2[0-4]\\d|25[0-5])\\.){3}([0-1]?\\d{1,2}|2[0-4]\\d|25[0-5])";
    @Setter
    @Accessors(chain = true)
    private String broadcastAddress;

    private Ipv4Network(Integer ifIndex, String ifName, String displayName, String macAddress, String cidrAddress,
                        String hostAddress, String broadcastAddress) {
        super(ifIndex, ifName, displayName, macAddress, cidrAddress, hostAddress);
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
        return new Ipv4Network(ni.getIndex(), ni.getName(), ni.getDisplayName(), mac(ni), cidr(ia),
                               ia.getAddress().getHostAddress(), ia.getBroadcast().getHostAddress());
    }

    public static List<Ipv4Network> getActiveIps() {
        return getActiveInterfaces(networkInterface -> true, Networks.IS_V4, Ipv4Network::from);
    }

    public static Ipv4Network getActiveIpByName(String interfaceName) {
        return getActiveIpByName(interfaceName, Networks.IS_V4, Ipv4Network::from);
    }

    public static Ipv4Network getFirstActiveIp() {
        return getFirstActiveIp(Networks.IS_V4, Ipv4Network::from);
    }

    public static Ipv4Network getActiveIpByBroadcast(@NonNull String broadcast) {
        return getActiveIpWithoutInterface(
            Networks.IS_V4.and(ia -> ia.getBroadcast().getHostAddress().equals(broadcast)), Ipv4Network::from);
    }

    private static String cidr(@NonNull InterfaceAddress interfaceAddress) {
        return cidr(interfaceAddress.getAddress(), interfaceAddress.getNetworkPrefixLength());
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
    int version() {
        return 4;
    }

    @Override
    protected Ipv4Network reload(@NonNull Ipv4Network network) {
        return super.reload(network).setBroadcastAddress(network.getBroadcastAddress());
    }

    @Override
    public Ipv4Network isReachable() throws CommunicationProtocolException {
        if (Strings.isBlank(getIfName()) && Strings.isBlank(getCidrAddress())) {
            return reload(Ipv4Network.getFirstActiveIp());
        }
        final Predicate<InterfaceAddress> iaPredicate = ia -> Optional.ofNullable(getCidrAddress())
                                                                      .map(cidr -> cidr.equals(cidr(ia)))
                                                                      .orElse(true);
        final List<Ipv4Network> interfaces = getActiveInterfaces(validateNetworkInterface(), iaPredicate,
                                                                 Ipv4Network::from);
        if (interfaces.size() != 1) {
            throw new CommunicationProtocolException("Interface name " + getIfName() + "is obsolete or down");
        }
        return reload(interfaces.get(0));
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends EthernetBuilder<Ipv4Network, Builder> {

        @Override
        public Ipv4Network build() {
            return new Ipv4Network(ifIndex(), ifName(), displayName(), macAddress(), cidrAddress(), hostAddress(),
                                   broadcastAddress);
        }

    }

}
