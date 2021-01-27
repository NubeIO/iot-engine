package com.nubeiot.edge.connector.bacnet.service.discovery;

import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.dto.msg.RequestData;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryLevel;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryOptions;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryRequest;
import com.nubeiot.edge.connector.bacnet.discovery.DiscoveryRequest.Fields;
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
        return Fields.networkCode;
    }

    @Override
    public Single<JsonObject> discoverThenWatch(@NonNull RequestData data) {
        return null;
    }

    @Override
    public Single<JsonObject> discoverManyThenWatch(@NonNull RequestData data) {
        return null;
    }

    @Override
    public Single<JsonObject> discover(RequestData reqData) {
        return Single.just(DiscoveryResponse.builder().network(parseProtocol(reqData)).build().toJson());
    }

    @Override
    public Single<JsonObject> discoverMany(RequestData reqData) {
        final DiscoveryOptions options = parseDiscoverOptions(reqData);
        final BACnetNetworkCache cache = networkCache();
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

    //    @Override
    //    public Single<JsonObject> discoverThenRegisterMany(RequestData reqData) {
    //        return discoverManyThenWatch(reqData.body());
    //    }

    //    @Override
    //    public Single<JsonObject> discoverThenRegisterOne(RequestData reqData) {
    //        final CommunicationProtocol protocol = parseProtocol(reqData);
    //        final BACnetNetworkCache cache = networkCache();
    //        final Optional<UUID> networkId = cache.getDataKey(protocol.identifier());
    //        if (networkId.isPresent()) {
    //            throw new AlreadyExistException(
    //                "Already persisted network code " + protocol.identifier() + " with id " + networkId.get());
    //        }
    //        final JsonObject network = JsonPojo.from(new BACnetNetworkTranslator().serialize(protocol)).toJson();
    //        return doPersist(network).doOnSuccess(response -> cache.addDataKey(protocol, parsePersistResponse
    //        (response)));
    //    }

    @Override
    protected String parseResourceId(@NonNull JsonObject resource) {
        return resource.getString("id");
    }

    //TODO need to split case physical network like ip and transport network like udp. For example: `ipv4-eth0`,
    // `udp4-eth0-47808`, `udp4-eth0-47809`
    private CommunicationProtocol parseProtocol(RequestData reqData) {
        return parseNetworkProtocol(DiscoveryRequest.from(reqData, DiscoveryLevel.NETWORK));
    }

    @Override
    public @NonNull String destination() {
        return null;
    }

}
