package com.nubeiot.edge.connector.bacnet.cache;

import com.nubeiot.core.cache.CacheInitializer;
import com.nubeiot.core.utils.Strings;
import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetVerticle;
import com.nubeiot.edge.module.datapoint.rpc.DataProtocolRpcClient;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BACnetCacheInitializer implements CacheInitializer<BACnetCacheInitializer, BACnetVerticle> {

    public static final String EDGE_NETWORK_CACHE = "EDGE_NETWORK_CACHE";
    public static final String BACNET_DEVICE_CACHE = "BACNET_DEVICE_CACHE";
    @NonNull
    @Getter
    private final BACnetConfig config;

    @Override
    public BACnetCacheInitializer init(@NonNull BACnetVerticle context) {
        context.addSharedData(DataProtocolRpcClient.GATEWAY_ADDRESS,
                              Strings.requireNotBlank(config.getGatewayAddress(), "Missing gateway address config"));
        addBlockingCache(context.getVertx(), EDGE_NETWORK_CACHE, BACnetNetworkCache::init, context::addSharedData);
        addBlockingCache(context.getVertx(), BACNET_DEVICE_CACHE,
                         () -> BACnetDeviceCache.init(context.getVertx(), context.getSharedKey()),
                         context::addSharedData);
        return this;
    }

}
