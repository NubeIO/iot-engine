package com.nubeiot.core.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import com.nubeiot.core.exceptions.NetworkException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Networks {

    public static final Predicate<InetAddress> IS_NAT_V4 = address -> !address.isAnyLocalAddress() &&
                                                                      !address.isMulticastAddress() &&
                                                                      !(address instanceof Inet6Address) &&
                                                                      !address.isLoopbackAddress();
    public static final Predicate<InterfaceAddress> IS_V4 = address -> IS_NAT_V4.test(address.getAddress());

    static final String CLUSTER_PUBLIC_PROP = "nube.cluster.public";
    static final String CLUSTER_PUBLIC_HOST_PROP = "nube.cluster.public.host";
    static final String CLUSTER_PUBLIC_PORT_PROP = "nube.cluster.public.port";
    static final String CLUSTER_PUBLIC_EVENTBUS_PORT_PROP = "nube.cluster.public.eventbus.port";
    private static final Logger logger = LoggerFactory.getLogger(Networks.class);
    private static final List<String> BLACK_LIST_ADDRESS = Arrays.asList("0.0.0.0", "127.0.0.1", "localhost");
    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private static String natHost = "";
    private static InetSocketAddress publicClusterAddr = null;
    private static InetSocketAddress publicClusterEventbusAddr = null;

    public static int validPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Cluster public port is not in range [1, 65535]");
        }
        return port;
    }

    public static int validPort(int port, int fallback) {
        return port < 1 || port > 65535 ? validPort(fallback) : port;
    }

    public static String computeNATAddress(String givenHost) {
        if (Objects.nonNull(natHost)) {
            return natHost;
        }
        synchronized (Networks.class) {
            if (Objects.nonNull(natHost)) {
                return natHost;
            }
            return getAddress(givenHost);
        }
    }

    public static InetSocketAddress computeClusterPublicUrl(String givenAddress) {
        if (Strings.isBlank(givenAddress)) {
            return computeClusterPublicUrl(null, -1);
        }
        final int index = givenAddress.lastIndexOf(":");
        if (index == -1) {
            logger.warn("Configured public cluster address is not valid: {}. Try to use environment", givenAddress);
            return computeClusterPublicUrl(null, -1);
        }
        return computeClusterPublicUrl(givenAddress.substring(0, index),
                                       Strings.convertToInt(givenAddress.substring(index + 1), -1));
    }

    public static InetSocketAddress computeClusterPublicUrl(String givenHost, int givenPort) {
        return computeAddress(givenHost, givenPort);
    }

    private static InetSocketAddress computeAddress(String givenHost, int givenPort) {
        boolean isPublicCluster = Boolean.valueOf(System.getProperty(CLUSTER_PUBLIC_PROP));
        if (!isPublicCluster) {
            return null;
        }
        if (Objects.nonNull(publicClusterAddr)) {
            return publicClusterAddr;
        }
        synchronized (Networks.class) {
            if (Objects.nonNull(publicClusterAddr)) {
                return publicClusterAddr;
            }
            publicClusterAddr = computeAddress(givenHost, givenPort, CLUSTER_PUBLIC_PORT_PROP);
            return publicClusterAddr;
        }
    }

    public static InetSocketAddress computeClusterEventbusUrl(String givenHost, int givenPort) {
        boolean isPublicCluster = Boolean.valueOf(System.getProperty(CLUSTER_PUBLIC_PROP));
        if (!isPublicCluster) {
            return null;
        }
        if (Objects.nonNull(publicClusterEventbusAddr)) {
            return publicClusterEventbusAddr;
        }
        synchronized (Networks.class) {
            if (Objects.nonNull(publicClusterEventbusAddr)) {
                return publicClusterEventbusAddr;
            }
            publicClusterEventbusAddr = computeAddress(givenHost, givenPort, CLUSTER_PUBLIC_EVENTBUS_PORT_PROP);
            return publicClusterEventbusAddr;
        }
    }

    synchronized static void cleanup() {
        natHost = null;
        publicClusterAddr = null;
        publicClusterEventbusAddr = null;
    }

    private static InetSocketAddress computeAddress(String givenHost, int givenPort, String portProperty) {
        String clusterHost = System.getProperty(CLUSTER_PUBLIC_HOST_PROP);
        clusterHost = Strings.requireNotBlank(Strings.isBlank(clusterHost) ? givenHost : clusterHost,
                                              "Cluster Public port is not defined");
        int clusterPort = validPort(Strings.convertToInt(System.getProperty(portProperty), givenPort));
        return InetSocketAddress.createUnresolved(clusterHost, clusterPort);
    }

    private static String getAddress(String givenHost) {
        if (!Strings.isBlank(givenHost) && !BLACK_LIST_ADDRESS.contains(givenHost)) {
            return givenHost;
        }
        Enumeration<NetworkInterface> nets = getNetworkInterfaces();
        List<InetAddress> usableINetAddresses = new ArrayList<>();
        while (nets.hasMoreElements()) {
            NetworkInterface networkInterface = nets.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                logger.debug("Found INetAddress: {} on interface: {}", address.toString(), networkInterface.getName());
                if (IS_NAT_V4.test(address)) {
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
        return Objects.isNull(loopbackAddress) ? DEFAULT_ADDRESS : loopbackAddress.getHostAddress();
    }

    private static Enumeration<NetworkInterface> getNetworkInterfaces() {
        try {
            return NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new NetworkException("Cannot get the network interfaces", e);
        }
    }

    public static InterfaceAddress findByName(String networkName) {
        try {
            NetworkInterface byName = NetworkInterface.getByName(Strings.requireNotBlank(networkName));
            if (Objects.isNull(byName)) {
                throw new NetworkException("Not found network interface by name " + networkName);
            }
            return byName.getInterfaceAddresses()
                         .stream()
                         .filter(IS_V4)
                         .findFirst()
                         .orElseThrow(() -> new NetworkException(
                             "Network interface address " + networkName + " is not valid IPv4"));
        } catch (SocketException e) {
            throw new NetworkException("Not found network interface by name " + networkName);
        }
    }

    public static InterfaceAddress firstNATIPv4() {
        Enumeration<NetworkInterface> nets = getNetworkInterfaces();
        while (nets.hasMoreElements()) {
            NetworkInterface networkInterface = nets.nextElement();
            final Optional<InterfaceAddress> first = networkInterface.getInterfaceAddresses()
                                                                     .stream()
                                                                     .filter(IS_V4)
                                                                     .findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        throw new NetworkException("Cannot find any IPv4 network interface");
    }

}
