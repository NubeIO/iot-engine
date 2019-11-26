package com.nubeiot.edge.connector.bacnet.service.discover;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;

import lombok.NonNull;

public final class NetworkDiscovery extends AbstractBACnetDiscoveryService implements BACnetDiscoveryService {

    public NetworkDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull String servicePath() {
        return "/discovery/bacnet/network";
    }

    @Override
    public String paramPath() {
        return Fields.networkCode;
    }

    @Override
    public Single<JsonObject> list(RequestData reqData) {
        final DiscoverOptions options = parseDiscoverOptions(reqData);
        final BACnetNetworkCache cache = getSharedDataValue(BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        if (options.isForce()) {
            BACnetNetworkCache.rescan(cache);
        }
        return Observable.fromIterable(cache.all().entrySet())
                         .groupBy(entry -> entry.getValue().type())
                         .flatMapSingle(m -> m.collect(JsonObject::new,
                                                       (json, net) -> json.put(net.getKey(), net.getValue().toJson()))
                                              .map(r -> new JsonObject().put(m.getKey(), r)))
                         .reduce(new JsonObject(), JsonObject::mergeIn);
    }

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        return Single.just(DiscoverResponse.builder().network(parseProtocol(reqData)).build().toJson());
    }

    @Override
    public Single<JsonObject> batchPersist(RequestData reqData) {
        return Single.just(new JsonObject());
    }

    @Override
    public Single<JsonObject> persist(RequestData reqData) {
        return doPersist(EventAction.CREATE, new BACnetNetworkTranslator().serialize(parseProtocol(reqData)).toJson());
    }

    @Override
    public String destination() {
        return DataPointIndex.lookupApiName(NetworkMetadata.INSTANCE);
    }

    private CommunicationProtocol parseProtocol(RequestData reqData) {
        return parseNetworkProtocol(DiscoverRequest.from(reqData, DiscoverLevel.NETWORK));
    }

}
