package com.nubeiot.core.protocol.network;

import java.util.Optional;

import com.nubeiot.core.utils.Strings;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
abstract class TransportBuilder<T extends TransportProtocol, B extends TransportBuilder> extends EthernetBuilder<T, B> {

    private IpNetwork ip;
    private int port;
    private boolean canReusePort;
    private String broadcastAddress;

    public B ip(IpNetwork ip) {
        this.ip = ip;
        return (B) this;
    }

    public B port(int port) {
        this.port = port;
        return (B) this;
    }

    public B canReusePort(boolean canReusePort) {
        this.canReusePort = canReusePort;
        return (B) this;
    }

    public B broadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
        return (B) this;
    }

    IpNetwork buildIp() {
        if (Optional.ofNullable(type()).map(s -> s.endsWith("4")).orElse(false) ||
            Strings.isNotBlank(broadcastAddress)) {
            return Ipv4Network.builder()
                              .ifIndex(ifIndex())
                              .ifName(ifName())
                              .displayName(displayName())
                              .macAddress(macAddress())
                              .cidrAddress(cidrAddress())
                              .hostAddress(hostAddress())
                              .broadcastAddress(broadcastAddress)
                              .build();
        }
        return Ipv6Network.builder()
                          .ifIndex(ifIndex())
                          .ifName(ifName())
                          .displayName(displayName())
                          .macAddress(macAddress())
                          .cidrAddress(cidrAddress())
                          .hostAddress(hostAddress())
                          .build();
    }

}
