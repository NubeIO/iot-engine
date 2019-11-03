package com.nubeiot.core.protocol.network;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nubeiot.core.dto.JsonData;
import com.nubeiot.core.exceptions.CommunicationProtocolException;
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
public abstract class TransportProtocol implements Ethernet {

    @NonNull
    @JsonUnwrapped
    private IpNetwork ip;
    private int port;

    @JsonCreator
    public static TransportProtocol parse(@NonNull Map<String, Object> data) {
        final String type = Strings.requireNotBlank(data.get("type"), "Missing protocol type");
        if (type.startsWith("udp")) {
            return JsonData.from(data, UdpProtocol.class);
        }
        if (type.startsWith("tcp")) {
            return JsonData.from(data, TcpProtocol.class);
        }
        throw new IllegalArgumentException("Unsupported protocol " + type);
    }

    public static TransportProtocol parse(@NonNull String identifier) {
        final String[] splitter = identifier.split(SPLIT_CHAR, 3);
        if (!splitter[0].matches("(?i)(udp|tcp)([46])?")) {
            throw new IllegalArgumentException("Unsupported protocol " + splitter[0]);
        }
        final String ifName = Strings.requireNotBlank(Functions.getIfThrow(() -> splitter[1]).orElse(null),
                                                      "Missing network interface name");
        final int port = Functions.getIfThrow(() -> Functions.toInt().apply(splitter[2]))
                                  .map(Networks::validPort)
                                  .orElseThrow(() -> new IllegalArgumentException("Missing port"));
        final IpNetwork network = splitter[0].endsWith("6")
                                  ? Ipv6Network.getActiveIpByName(ifName)
                                  : Ipv4Network.getActiveIpByName(ifName);
        if (splitter[0].equalsIgnoreCase("udp")) {
            return UdpProtocol.builder().ip(network).port(port).build();
        }
        return TcpProtocol.builder().ip(network).port(port).build();
    }

    @Override
    public Integer getIfIndex() {
        return getIp().getIfIndex();
    }

    @Override
    public String getIfName() {
        return getIp().getIfName();
    }

    @Override
    public String getDisplayName() {
        return getIp().getDisplayName();
    }

    @Override
    public String getMacAddress() {
        return getIp().getMacAddress();
    }

    @Override
    public String getCidrAddress() {
        return getIp().getCidrAddress();
    }

    @Override
    public String getHostAddress() {
        return getIp().getHostAddress();
    }

    @Override
    public abstract @NonNull TransportProtocol isReachable() throws CommunicationProtocolException;

    @Override
    @EqualsAndHashCode.Include
    public @NonNull String identifier() {
        return Ethernet.super.identifier() + SPLIT_CHAR + getPort();
    }

}
