package com.nubeiot.edge.connector.bacnet.Util;

import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/*
 * Utils for getting network addresses etc
 */


public class NetworkUtils {

    private static InterfaceAddress interfaceAddress = null;

    private static InterfaceAddress getFirstAddress(String networkInterfaceString) throws java.net.SocketException {
        if (interfaceAddress != null) {
            return interfaceAddress;
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback()) {
                continue;
            }
            if (networkInterface == null || networkInterface.getDisplayName().equals(networkInterfaceString)) {
                for (InterfaceAddress intAddress : networkInterface.getInterfaceAddresses()) {
                    if (intAddress.getAddress() == null && intAddress.getBroadcast() == null) {
                        continue;
                    }
                    if (intAddress.getAddress() instanceof Inet6Address) {
                        continue;
                    }

                    interfaceAddress = intAddress;
                    return interfaceAddress;
                }
            }
        }
        throw new java.net.SocketException();
    }

    public static String getBroadcastAddress(String networkInterface) throws java.net.SocketException {
        return getFirstAddress(networkInterface).getBroadcast().getHostAddress();
    }

    public static String getIPv4Address(String networkInterface) throws java.net.SocketException {
        return getFirstAddress(networkInterface).getAddress().getHostAddress();
    }

    public static short getNetworkPrefixLength(String networkInterface) throws java.net.SocketException {
        return getFirstAddress(networkInterface).getNetworkPrefixLength();
    }

}
