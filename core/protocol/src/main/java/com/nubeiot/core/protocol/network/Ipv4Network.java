package com.nubeiot.core.protocol.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.nubeiot.core.exceptions.CommunicationProtocolException;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Ipv4Network.Builder.class)
public final class Ipv4Network extends IpNetwork implements Ethernet {

    private final String broadcastAddress;

    private Ipv4Network(int index, String name, String displayName, String macAddress, String cidrAddress,
                        String hostAddress, String broadcastAddress) {
        super(index, name, displayName, macAddress, cidrAddress, hostAddress);
        this.broadcastAddress = broadcastAddress;
    }

    public static Ipv4Network from(@NonNull NetworkInterface ni, @NonNull InterfaceAddress ia) {
        return new Ipv4Network(ni.getIndex(), ni.getName(), ni.getDisplayName(), mac(ni),
                               cidr(ia.getAddress(), ia.getNetworkPrefixLength()), ia.getAddress().getHostAddress(),
                               ia.getBroadcast().getHostAddress());
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

    private static int mask(final int length) {
        int l = 0;
        int shift = 31;
        for (int i = 0; i < length; i++) {
            l |= 1 << shift--;
        }
        return l;
    }

    private static int byteArrayToInt(byte[] ba) {
        return ba[3] & 0xFF | (ba[2] & 0xFF) << 8 | (ba[1] & 0xFF) << 16 | (ba[0] & 0xFF) << 24;
    }

    private static int[] intToByteArray(int a) {
        return new int[] {(a >> 24) & 0xFF, (a >> 16) & 0xFF, (a >> 8) & 0xFF, a & 0xFF};
    }

    private static String format(int[] octets) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < octets.length; ++i) {
            str.append(octets[i]);
            if (i != octets.length - 1) {
                str.append(".");
            }
        }
        return str.toString();
    }

    private static String cidr(@NonNull InetAddress ia, short prefixLength) {
        return format(intToByteArray(byteArrayToInt(ia.getAddress()) & mask(prefixLength))) + "/" + prefixLength;
    }

    @Override
    public Ipv4Network isReachable() throws CommunicationProtocolException {
        return this;
    }

    @Override
    public String identifier() {
        return type() + "-";
    }

    @Override
    int version() {
        return 4;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends IpBuilder<Ipv4Network, Builder> {

        @Override
        public Ipv4Network build() {
            return new Ipv4Network(index(), name(), displayName(), macAddress(), cidrAddress(), hostAddress(),
                                   broadcastAddress);
        }

    }

}
