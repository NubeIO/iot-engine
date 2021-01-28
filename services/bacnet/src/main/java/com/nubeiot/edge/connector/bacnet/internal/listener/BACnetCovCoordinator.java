package com.nubeiot.edge.connector.bacnet.internal.listener;

import io.github.zero88.qwe.component.SharedDataLocalProxy;

import com.nubeiot.core.rpc.watcher.WatcherOption;
import com.nubeiot.edge.connector.bacnet.service.InboundBACnetCoordinator;
import com.nubeiot.iotdata.IoTEntity;

import lombok.NonNull;

public final class BACnetCovCoordinator implements InboundBACnetCoordinator<IoTEntity> {

    @Override
    public @NonNull Class<IoTEntity> context() {
        return null;
    }

    @Override
    public @NonNull SharedDataLocalProxy sharedData() {
        return null;
    }

    @Override
    public @NonNull String destination() {
        return null;
    }

    @Override
    public WatcherOption option() {
        return null;
    }

}
