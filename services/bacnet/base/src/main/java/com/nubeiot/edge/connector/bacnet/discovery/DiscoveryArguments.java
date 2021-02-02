package com.nubeiot.edge.connector.bacnet.discovery;

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
    @NonNull
    private final DiscoveryLevel level;

}
