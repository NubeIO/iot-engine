package com.nubeiot.edge.connector.bacnet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.vertx.core.Vertx;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.serotonin.bacnet4j.event.DeviceEventListener;

import lombok.Builder;
import lombok.NonNull;

@Builder(builderClassName = "Builder")
public final class BACnetDeviceInitializer {

    @NonNull
    private final Vertx vertx;
    @NonNull
    private final String sharedKey;
    private final Consumer<BACnetDevice> preFunction;
    private final List<DeviceEventListener> listeners;

    public BACnetDevice asyncStart(@NonNull CommunicationProtocol protocol) {
        final BACnetDevice device = new BACnetDevice(vertx, sharedKey, protocol);
        Optional.ofNullable(preFunction).ifPresent(f -> f.accept(device));
        Optional.ofNullable(listeners).map(l -> l.toArray(new DeviceEventListener[0])).ifPresent(device::addListener);
        return device.asyncStart();
    }

}
