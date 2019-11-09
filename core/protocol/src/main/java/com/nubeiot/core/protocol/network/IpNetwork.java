package com.nubeiot.core.protocol.network;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.CommunicationProtocolException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.utils.Networks;
import com.nubeiot.core.utils.Strings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter(value = AccessLevel.PROTECTED)
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class IpNetwork<T extends IpNetwork> implements Ethernet {

    private Integer ifIndex;
    private String ifName;
    private String displayName;
    private String macAddress;
    private String cidrAddress;
    private String hostAddress;

    @JsonCreator
    public static IpNetwork parse(@NonNull Map<String, Object> data) {
        final String type = Strings.requireNotBlank(data.get("type"), "Missing protocol type");
        if (type.equalsIgnoreCase("ipv6")) {
            return JsonData.from(data, Ipv6Network.class);
        }
        return JsonData.from(data, Ipv4Network.class);
    }

    public static IpNetwork parse(@NonNull String identifier) {
        String[] splitter = identifier.split(SPLIT_CHAR, 2);
        if (splitter[0].equalsIgnoreCase("ipv4")) {
            return Ipv4Network.getActiveIpByName(getInterfaceName(splitter[1]));
        }
        if (splitter[0].equalsIgnoreCase("ipv6")) {
            return Ipv6Network.getActiveIpByName(getInterfaceName(splitter[1]));
        }
        return Ipv4Network.getActiveIpByName(getInterfaceName(splitter[0]));
    }

    static <T extends IpNetwork> List<T> getActiveInterfaces(@NonNull Predicate<NetworkInterface> interfacePredicate,
                                                             @NonNull Predicate<InterfaceAddress> addressPredicate,
                                                             @NonNull BiFunction<NetworkInterface, InterfaceAddress,
                                                                                    T> parser) {
        List<T> list = new ArrayList<>();
        Enumeration<NetworkInterface> nets = Networks.getNetworkInterfaces();
        while (nets.hasMoreElements()) {
            final NetworkInterface networkInterface = nets.nextElement();
            try {
                if (!networkInterface.isUp() || !interfacePredicate.test(networkInterface)) {
                    continue;
                }
            } catch (SocketException ignored) {
            }
            networkInterface.getInterfaceAddresses()
                            .stream()
                            .filter(addressPredicate)
                            .findFirst()
                            .map(interfaceAddress -> parser.apply(networkInterface, interfaceAddress))
                            .ifPresent(list::add);
        }
        return list;
    }

    static <T extends IpNetwork> T getActiveIpByName(String interfaceName,
                                                     @NonNull Predicate<InterfaceAddress> iaPredicate,
                                                     @NonNull BiFunction<NetworkInterface, InterfaceAddress, T> parser) {
        return getActiveInterfaces(ni -> ni.getName().equalsIgnoreCase(getInterfaceName(interfaceName)), iaPredicate,
                                   parser).stream().findFirst().orElseThrow(notFound(interfaceName));
    }

    static <T extends IpNetwork> T getFirstActiveIp(@NonNull Predicate<InterfaceAddress> iaPredicate,
                                                    @NonNull BiFunction<NetworkInterface, InterfaceAddress, T> parser) {
        return getActiveInterfaces(ni -> true, iaPredicate, parser).stream().findFirst().orElseThrow(notFound(null));
    }

    static <T extends IpNetwork> T getActiveIpWithoutInterface(@NonNull Predicate<InterfaceAddress> iaPredicate,
                                                               @NonNull BiFunction<NetworkInterface, InterfaceAddress
                                                                                      , T> parser) {
        return getActiveInterfaces(ni -> true, iaPredicate, parser).stream().findFirst().orElseThrow(notFound(null));
    }

    private static Supplier<NotFoundException> notFound(String interfaceName) {
        return () -> new NotFoundException("Not found active IP network interface" +
                                           Optional.ofNullable(interfaceName).map(n -> " with name " + n).orElse(""));
    }

    private static String getInterfaceName(String interfaceName) {
        return Strings.requireNotBlank(interfaceName, "Missing interface name");
    }

    static String mac(@NonNull NetworkInterface networkInterface) {
        final byte[] mac;
        try {
            mac = networkInterface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (SocketException | NullPointerException e) {
            return null;
        }
    }

    static int mask(final int length) {
        int l = 0;
        int shift = 31;
        for (int i = 0; i < length; i++) {
            l |= 1 << shift--;
        }
        return l;
    }

    abstract int version();

    @SuppressWarnings("unchecked")
    protected T reload(@NonNull T network) {
        return (T) this.setIfIndex(network.getIfIndex())
                       .setIfName(network.getIfName())
                       .setDisplayName(network.getDisplayName())
                       .setMacAddress(network.getMacAddress())
                       .setCidrAddress(network.getCidrAddress())
                       .setHostAddress(network.getHostAddress());
    }

    @Override
    public final @NonNull String type() {
        return "ipv" + version();
    }

    @Override
    public abstract T isReachable() throws CommunicationProtocolException;

    @Override
    @EqualsAndHashCode.Include
    public @NonNull String identifier() {
        return Ethernet.super.identifier();
    }

    @JsonIgnore
    public String getSubnetAddress() {
        return cidrAddress.substring(0, cidrAddress.lastIndexOf("/"));
    }

    @JsonIgnore
    public short getSubnetPrefixLength() {
        return Short.parseShort(cidrAddress.substring(cidrAddress.lastIndexOf("/") + 1));
    }

    Predicate<NetworkInterface> validateNetworkInterface() {
        return ni ->
                   Optional.ofNullable(getIfName()).map(ifName -> ifName.equalsIgnoreCase(ni.getName())).orElse(true) &&
                   Optional.ofNullable(mac(ni))
                           .flatMap(mac -> Optional.ofNullable(getMacAddress())
                                                   .map(givenMac -> givenMac.equalsIgnoreCase(mac)))
                           .orElse(true);
    }

}
