package com.nubeiot.edge.connector.bacnet.Util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/*
 * Utils for getting network addresses etc
 */

public class NetworkUtils {

    private static InterfaceAddress interfaceAddress = null;

    private static InterfaceAddress getFirstAddress() throws java.net.SocketException{
        if(interfaceAddress != null){
            return interfaceAddress;
        }
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements())
        {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback())
                continue;
            for (InterfaceAddress intAddress : networkInterface.getInterfaceAddresses())
            {
                if (intAddress.getAddress() == null && intAddress.getBroadcast() == null)
                    continue;
                if (intAddress.getAddress() instanceof Inet6Address)
                    continue;

                interfaceAddress = intAddress;
                return interfaceAddress;
            }
        }
        throw new java.net.SocketException();
    }

    public static String getBroadcastAddress() throws java.net.SocketException{
        return getFirstAddress().getBroadcast().getHostAddress();
    }

    public static String getIPv4Address() throws java.net.SocketException{
        return getFirstAddress().getAddress().getHostAddress();
    }

    public static short getNetworkPrefixLength() throws java.net.SocketException{
        return getFirstAddress().getNetworkPrefixLength();
    }
}
