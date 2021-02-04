package com.nubeiot.edge.connector.bacnet.service;

import java.util.Optional;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.iot.connector.BaseService;
import io.github.zero88.qwe.protocol.CommunicationProtocol;

import com.nubeiot.edge.connector.bacnet.BACnetConfig;
import com.nubeiot.edge.connector.bacnet.BACnetDevice;
import com.nubeiot.edge.connector.bacnet.BACnetProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryArguments;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;

import lombok.NonNull;

public abstract class AbstractBACnetService extends BaseService implements BACnetProtocol {

    protected AbstractBACnetService(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    protected final BACnetNetworkCache networkCache() {
        return sharedData().getData(BACnetCacheInitializer.LOCAL_NETWORK_CACHE);
    }

    protected final BACnetDeviceCache deviceCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
    }

    protected final BACnetObjectCache objectCache() {
        return sharedData().getData(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
    }

    //TODO it must be override in each BACnet service to manage cache time
    protected DiscoveryOptions parseDiscoverOptions(@NonNull RequestData reqData) {
        final BACnetConfig config = sharedData().getData(BACnetDevice.CONFIG_KEY);
        return DiscoveryOptions.from(config.getMaxTimeoutInMS(), reqData);
    }

    protected final CommunicationProtocol parseNetworkProtocol(@NonNull DiscoveryParams params) {
        return Optional.ofNullable(params.getNetwork())
                       .map(n -> BACnetNetwork.factory(n).toProtocol())
                       .map(p -> networkCache().add(p.identifier(), p).get(p.identifier()))
                       .orElseGet(() -> networkCache().get(params.getNetworkId()));
    }

    protected final @NonNull DiscoveryArguments createDiscoveryArgs(@NonNull RequestData reqData,
                                                                    @NonNull DiscoveryLevel level) {
        return new DiscoveryArguments(DiscoveryParams.from(reqData, level), parseDiscoverOptions(reqData), level);
    }

    protected final @NonNull BACnetDevice getLocalDeviceFromCache(@NonNull DiscoveryArguments request) {
        return deviceCache().get(parseNetworkProtocol(request.params()));
    }

}
