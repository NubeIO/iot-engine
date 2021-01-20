package com.nubeiot.edge.connector.bacnet.handler;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import io.github.zero88.msa.bp.component.SharedDataDelegate;
import io.github.zero88.msa.bp.dto.ErrorData;
import io.github.zero88.msa.bp.dto.JsonData;
import io.github.zero88.msa.bp.dto.msg.RequestData;
import io.github.zero88.msa.bp.utils.ExecutorHelpers;
import io.github.zero88.utils.UUID64;
import io.reactivex.Observable;
import io.vertx.core.Vertx;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetObjectCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.service.scanner.BACnetPointScanner;
import com.nubeiot.edge.connector.bacnet.service.scanner.BACnetScannerHelper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BACnetDiscoverFinisher extends DiscoverCompletionHandler
    implements SharedDataDelegate<BACnetDiscoverFinisher> {

    private final Vertx vertx;
    private final String sharedKey;

    @Override
    public <D> D getSharedDataValue(String dataKey) {
        return SharedDataDelegate.getLocalDataValue(vertx, sharedKey, dataKey);
    }

    @Override
    public BACnetDiscoverFinisher registerSharedData(@NonNull Function<String, Object> sharedDataFunc) {
        return this;
    }

    //TODO need scan network to find device/point then caching data
    @Override
    public boolean success(@NonNull RequestData requestData) {
        final DiscoverResponse info = JsonData.from(requestData.body(), DiscoverResponse.class);
        final BACnetNetworkCache networkCache = getSharedDataValue(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        final BACnetDeviceCache deviceCache = getSharedDataValue(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        final BACnetObjectCache objectCache = getSharedDataValue(BACnetCacheInitializer.BACNET_OBJECT_CACHE);
        final CommunicationProtocol protocol = networkCache.get(info.getNetwork().identifier());
        final Optional<UUID> optional = networkCache.getDataKey(protocol.identifier());
        if (!optional.isPresent()) {
            return super.success(requestData);
        }
        final UUID networkId = optional.get();
        final BACnetPointScanner pointScanner = BACnetScannerHelper.createPointScanner(vertx, sharedKey);
        ExecutorHelpers.blocking(vertx, () -> BACnetScannerHelper.createDeviceScanner(vertx, sharedKey))
                       .flatMap(deviceScanner -> deviceScanner.scan(networkId))
                       .flatMapObservable(map -> Observable.fromIterable(map.entrySet()))
                       .doOnNext(
                           entry -> deviceCache.addDataKey(protocol, entry.getValue().getObjectId(), entry.getKey()))
                       .flatMapSingle(entry -> pointScanner.scan(networkId, UUID64.uuid64ToUuid(entry.getKey())))
                       .subscribe(logger::info, logger::error);
        return true;
    }

    //TODO need disabled network and corresponding device/point, remove cache???
    @Override
    public boolean error(@NonNull ErrorData error) {
        final DiscoverResponse extraInfo = JsonData.from(error.getExtraInfo(), DiscoverResponse.class);
        return super.error(error);
    }

}
