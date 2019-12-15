package com.nubeiot.edge.connector.bacnet.cache;

import com.nubeiot.core.cache.AbstractLocalCache;
import com.nubeiot.core.cache.LocalDataCache;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.protocol.network.Ipv4Network;
import com.nubeiot.core.protocol.network.Ipv6Network;
import com.nubeiot.core.protocol.serial.SerialPortProtocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BACnetNetworkCache extends AbstractLocalCache<String, CommunicationProtocol, BACnetNetworkCache>
    implements LocalDataCache<String, CommunicationProtocol> {

    static BACnetNetworkCache init() {
        return rescan(new BACnetNetworkCache().register(CommunicationProtocol::parse));
    }

    public static BACnetNetworkCache rescan(@NonNull BACnetNetworkCache cache) {
        Ipv4Network.getActiveIps().forEach(ipv4 -> cache.add(ipv4.identifier(), ipv4));
        Ipv6Network.getActiveIps().forEach(ipv6 -> cache.add(ipv6.identifier(), ipv6));
        SerialPortProtocol.getActivePorts().forEach(serialPort -> cache.add(serialPort.identifier(), serialPort));
        return cache;
    }

    @Override
    protected String keyClass() {
        return String.class.getName();
    }

    @Override
    protected String valueClass() {
        return Ipv4Network.class.getSimpleName();
    }

    @Override
    public BACnetNetworkCache add(@NonNull String key, CommunicationProtocol protocol) {
        cache().put(key, protocol);
        return this;
    }

}
