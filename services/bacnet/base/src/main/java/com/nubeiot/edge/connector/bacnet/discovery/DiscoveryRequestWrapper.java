package com.nubeiot.edge.connector.bacnet.discovery;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class DiscoveryRequestWrapper {

    @NonNull
    private final DiscoveryRequest request;
    @NonNull
    private final DiscoveryOptions options;
    private final BACnetDevice device;

    public ObjectIdentifier remoteDeviceId() {
        return request.getDeviceCode();
    }

    public ObjectIdentifier objectCode() {
        return request.getObjectId();
    }

}
