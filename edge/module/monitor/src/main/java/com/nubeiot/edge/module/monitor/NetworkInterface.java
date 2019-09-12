package com.nubeiot.edge.module.monitor;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nubeiot.core.dto.JsonData;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import oshi.hardware.NetworkIF;
import oshi.util.FormatUtil;

@Getter
@RequiredArgsConstructor
@Builder(builderClassName = "Builder")
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public final class NetworkInterface implements JsonData {

    final String name;
    final String displayName;
    final String macAddress;
    final int mtu;
    final String speed;
    final String[] ipv4;
    final String[] ipv6;
    final String traffic;

    public static NetworkInterface from(NetworkIF net) {
        boolean hasData = net.getBytesRecv() > 0 || net.getBytesSent() > 0 || net.getPacketsRecv() > 0 ||
                          net.getPacketsSent() > 0;

        String traffic = String.format("received %s/%s%s; transmitted %s/%s%s",
                                       hasData ? net.getPacketsRecv() + " packets" : "?",
                                       hasData ? FormatUtil.formatBytes(net.getBytesRecv()) : "?",
                                       hasData ? " (" + net.getInErrors() + " err)" : "",
                                       hasData ? net.getPacketsSent() + " packets" : "?",
                                       hasData ? FormatUtil.formatBytes(net.getBytesSent()) : "?",
                                       hasData ? " (" + net.getOutErrors() + " err)" : "");

        return NetworkInterface.builder()
                               .name(net.getName())
                               .displayName(net.getDisplayName())
                               .macAddress(net.getMacaddr())
                               .mtu(net.getMTU())
                               .speed(FormatUtil.formatValue(net.getSpeed(), "bps"))
                               .ipv4(net.getIPv4addr())
                               .ipv6(net.getIPv6addr())
                               .traffic(traffic)
                               .build();
    }

}
