package com.nubeiot.edge.connector.bacnet.mixin;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import com.nubeiot.core.dto.JsonData;

import lombok.Getter;
import lombok.NonNull;

@Getter
public final class NetworkInterfaceWrapper implements JsonData {

    private final int index;
    private final String name;
    private final String displayName;
    private final String macAddress;
    private final String hostAddress;
    private final String broadcastAddress;
    private final short netMask;

    public NetworkInterfaceWrapper(@NonNull NetworkInterface ni, @NonNull InterfaceAddress ia) {
        this.index = ni.getIndex();
        this.name = ni.getName();
        this.displayName = ni.getDisplayName();
        this.macAddress = mac(ni);
        this.hostAddress = ia.getAddress().getHostAddress();
        this.broadcastAddress = ia.getBroadcast().getHostAddress();
        this.netMask = ia.getNetworkPrefixLength();
    }

    private static String mac(@NonNull NetworkInterface networkInterface) {
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

}
