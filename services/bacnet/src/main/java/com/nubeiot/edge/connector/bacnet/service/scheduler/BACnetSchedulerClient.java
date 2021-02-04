package com.nubeiot.edge.connector.bacnet.service.scheduler;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.iot.connector.rpc.scheduler.SchedulerClient;

import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.service.AbstractBACnetRpcClient;

import lombok.NonNull;

public final class BACnetSchedulerClient extends AbstractBACnetRpcClient implements SchedulerClient, BACnetProtocol {

    public BACnetSchedulerClient(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull String destination() {
        return sharedData().getData(BACnetCacheInitializer.SCHEDULER_SERVICE_NAME);
    }

}
