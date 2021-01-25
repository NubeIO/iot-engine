package com.nubeiot.edge.connector.bacnet.handler;

import java.util.Optional;
import java.util.UUID;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.ErrorData;
import io.github.zero88.qwe.dto.JsonData;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.github.zero88.qwe.utils.ExecutorHelpers;
import io.github.zero88.utils.UUID64;
import io.reactivex.Observable;

import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.service.scanner.BACnetPointScanner;
import com.nubeiot.edge.connector.bacnet.service.scanner.BACnetScannerHelper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class BACnetDiscoverFinisher extends DiscoverCompletionHandler {

    private final SharedDataLocalProxy proxy;

    //TODO need scan network to find device/point then caching data
    @Override
    public boolean success(@NonNull RequestData requestData) {
        final DiscoverResponse info = JsonData.from(requestData.body(), DiscoverResponse.class);
        final BACnetNetworkCache networkCache = proxy.getData(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        final BACnetDeviceCache deviceCache = proxy.getData(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetObjectCache objectCache = proxy.getData(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
        final CommunicationProtocol protocol = networkCache.get(info.getNetwork().identifier());
        final Optional<UUID> optional = networkCache.getDataKey(protocol.identifier());
        if (!optional.isPresent()) {
            return super.success(requestData);
        }
        final UUID networkId = optional.get();
        final BACnetPointScanner pointScanner = BACnetScannerHelper.createPointScanner(proxy);
        ExecutorHelpers.blocking(proxy.getVertx(), () -> BACnetScannerHelper.createDeviceScanner(proxy))
                       .flatMap(deviceScanner -> deviceScanner.scan(networkId))
                       .flatMapObservable(map -> Observable.fromIterable(map.entrySet()))
                       .doOnNext(
                           entry -> deviceCache.addDataKey(protocol, entry.getValue().getObjectId(), entry.getKey()))
                       .flatMapSingle(entry -> pointScanner.scan(networkId, UUID64.uuid64ToUuid(entry.getKey())))
                       .subscribe(s -> log.info("Finished BACnet scan [{}]", s.keySet()),
                                  err -> log.warn("Unable to scan network. Error: {}", err.getMessage(), err));
        return true;
    }

    //TODO need disabled network and corresponding device/point, remove cache???
    @Override
    public boolean error(@NonNull ErrorData error) {
        final DiscoverResponse extraInfo = JsonData.from(error.getExtraInfo(), DiscoverResponse.class);
        return super.error(error);
    }

}
