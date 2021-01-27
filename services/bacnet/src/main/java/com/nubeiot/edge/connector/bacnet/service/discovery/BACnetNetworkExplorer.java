package com.nubeiot.edge.connector.bacnet.service.discovery;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryParams;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryResponse;
import com.nubeiot.edge.connector.bacnet.entity.BACnetNetwork;

import lombok.NonNull;

public final class BACnetNetworkExplorer extends AbstractBACnetExplorer<BACnetNetwork>
    implements BACnetExplorer<BACnetNetwork> {

    BACnetNetworkExplorer(@NonNull SharedDataLocalProxy sharedDataProxy) {
        super(sharedDataProxy);
    }

    @Override
    public @NonNull Class<BACnetNetwork> context() {
        return BACnetNetwork.class;
    }

    @Override
    public @NonNull String servicePath() {
        return "/network";
    }

    @Override
    public String paramPath() {
        return DiscoveryParams.Fields.networkCode;
    }

    @Override
    public Single<JsonObject> discover(RequestData reqData) {
        return Single.just(DiscoveryParams.from(reqData, DiscoveryLevel.NETWORK))
                     .map(this::parseNetworkProtocol)
                     .map(cp -> DiscoveryResponse.builder().network(cp).build().toJson());
    }

    @Override
    public Single<JsonObject> discoverMany(RequestData reqData) {
        final DiscoveryOptions options = parseDiscoverOptions(reqData);
        final BACnetNetworkCache cache = networkCache();
        if (options.isForce()) {
            BACnetNetworkCache.rescan(cache);
        }
        return Observable.fromIterable(cache.all().entrySet()).groupBy(entry -> entry.getValue().type())
                         .flatMapSingle(m -> m.collect(JsonObject::new,
                                                       (json, net) -> json.put(net.getKey(), net.getValue().toJson()))
                                              .map(r -> new JsonObject().put(m.getKey(), r)))
                         .reduce(new JsonObject(), JsonObject::mergeIn);
    }

    @Override
    protected String parseResourceId(@NonNull JsonObject resource) {
        return resource.getString("id");
    }

}
