package com.nubeiot.core.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Networks {

    private static final Logger logger = LoggerFactory.getLogger(Networks.class);
    private static String host = "";

    public static String getDefaultAddress(String givenHost) {
        if (Strings.isNotBlank(host)) {
            return host;
        }
        synchronized (Networks.class) {
            host = getAddress(givenHost);
            return host;
        }
    }

    private static String getAddress(String givenHost) {
        if (!Strings.isBlank(givenHost) && !"0.0.0.0".equals(givenHost) && !"127.0.0.1".equals(givenHost)) {
            return givenHost;
        }
        Enumeration<NetworkInterface> nets;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.error("Cannot get the network interfaces ", e);
            return "0.0.0.0";
        }

        NetworkInterface networkInterface;
        List<InetAddress> usableINetAddresses = new ArrayList<>();
        while (nets.hasMoreElements()) {
            networkInterface = nets.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                logger.debug("Found INetAddress: {} on interface: {}", address.toString(), networkInterface.getName());
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress() &&
                    !(address instanceof Inet6Address) && !address.isLoopbackAddress()) {
                    usableINetAddresses.add(address);
                }
            }
        }

        if (usableINetAddresses.size() > 1) {
            throw new IllegalStateException(
                "Don't know which INetAddress to use, there are more than one: " + usableINetAddresses);
        } else if (usableINetAddresses.size() == 1) {
            logger.info("Found default INetAddress: {}", usableINetAddresses.get(0).toString());
            return usableINetAddresses.get(0).getHostAddress();
        }

        logger.warn("Not found usable INet address, fallback to loopback");
        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        return Objects.isNull(loopbackAddress) ? "0.0.0.0" : loopbackAddress.getHostAddress();
    }

}
