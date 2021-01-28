package com.nubeiot.edge.connector.bacnet.discovery;

import com.serotonin.bacnet4j.type.primitive.ObjectIdentifier;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class DiscoveryArguments {

    @NonNull
    private final DiscoveryParams params;
    @NonNull
    private final DiscoveryOptions options;

    public ObjectIdentifier remoteDeviceId() {
        return params.getDeviceCode();
    }

    public ObjectIdentifier objectCode() {
        return params.getObjectId();
    }

}
