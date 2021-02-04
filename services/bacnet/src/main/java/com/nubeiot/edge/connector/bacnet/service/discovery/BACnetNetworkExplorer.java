package com.nubeiot.edge.connector.bacnet.service.discovery;

import java.util.Map.Entry;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.entity.BACnetEntities.BACnetNetworks;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;

import lombok.NonNull;

public final class BACnetNetworkExplorer extends BACnetExplorer<String, BACnetNetwork, BACnetNetworks> {

    BACnetNetworkExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public Single<BACnetNetwork> discover(@NonNull RequestData reqData) {
        return Single.just(DiscoveryParams.from(reqData, level()))
                     .map(this::parseNetworkProtocol)
                     .map(BACnetNetwork::fromProtocol);
    }

    @Override
    public Single<BACnetNetworks> discoverMany(@NonNull RequestData reqData) {
        final DiscoveryOptions options = parseDiscoverOptions(reqData);
        final BACnetNetworkCache cache = networkCache();
        if (options.isForce()) {
            BACnetNetworkCache.rescan(cache);
        }
        return Observable.fromIterable(cache.all().entrySet())
                         .map(Entry::getValue)
                         .map(BACnetNetwork::fromProtocol)
                         .collect(BACnetNetworks::new, BACnetNetworks::add);
    }

    @Override
    public DiscoveryLevel level() {
        return DiscoveryLevel.NETWORK;
    }

}
