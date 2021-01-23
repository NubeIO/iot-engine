package com.nubeiot.edge.connector.bacnet.service.discover;

import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class DiscoveryRequestWrapper {

    @NonNull
    private final DiscoverRequest request;
    @NonNull
    private final DiscoverOptions options;
    private final BACnetDevice device;

    public ObjectIdentifier remoteDeviceId() {
        return request.getDeviceCode();
    }

    public ObjectIdentifier objectCode() {
        return request.getObjectId();
    }

}
