package com.nubeiot.core.protocol.network;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.utils.Functions;
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
public abstract class IpNetwork implements Ethernet {

    private Integer ifIndex;
    private String ifName;
    private String displayName;
    private String macAddress;
    private String cidrAddress;
    private String hostAddress;
    @JsonIgnore
    private short prefixLength;

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

    private static String getInterfaceName(String interfaceName) {
        return Functions.getOrThrow(() -> Strings.requireNotBlank(interfaceName),
                                    () -> new IllegalArgumentException("Invalid IP identifier"));
    }

    static List<IpNetwork> getActiveInterfaces(@NonNull Predicate<NetworkInterface> interfacePredicate,
                                               @NonNull Predicate<InterfaceAddress> addressPredicate,
                                               @NonNull BiFunction<NetworkInterface, InterfaceAddress, IpNetwork> parser) {
        List<IpNetwork> list = new ArrayList<>();
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

    static String mac(@NonNull NetworkInterface networkInterface) {
        final byte[] mac;
        try {
            mac = networkInterface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (SocketException e) {
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

    @Override
    public final @NonNull String type() {
        return "ipv" + version();
    }

    @Override
    @EqualsAndHashCode.Include
    public @NonNull String identifier() {
        return Ethernet.super.identifier();
    }

}
