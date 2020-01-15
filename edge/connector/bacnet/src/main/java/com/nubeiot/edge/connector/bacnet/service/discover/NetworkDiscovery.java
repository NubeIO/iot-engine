package com.nubeiot.edge.connector.bacnet.service.discover;

import java.util.Optional;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.exceptions.AlreadyExistException;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.core.sql.EntityMetadata;
import com.nubeiot.core.sql.pojos.JsonPojo;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverOptions;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.DiscoverLevel;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverRequest.Fields;
import com.nubeiot.edge.connector.bacnet.discover.DiscoverResponse;
import com.nubeiot.edge.connector.bacnet.translator.BACnetNetworkTranslator;
import com.nubeiot.edge.module.datapoint.DataPointIndex.NetworkMetadata;

import lombok.NonNull;

public final class NetworkDiscovery extends AbstractDiscoveryService implements BACnetDiscoveryService {

    NetworkDiscovery(@NonNull Vertx vertx, @NonNull String sharedKey) {
        super(vertx, sharedKey);
    }

    @Override
    public @NonNull EntityMetadata context() {
        return NetworkMetadata.INSTANCE;
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
    public Single<JsonObject> list(RequestData reqData) {
        final DiscoverOptions options = parseDiscoverOptions(reqData);
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

    @Override
    public Single<JsonObject> get(RequestData reqData) {
        return Single.just(DiscoverResponse.builder().network(parseProtocol(reqData)).build().toJson());
    }

    @Override
    public Single<JsonObject> discoverThenDoBatch(RequestData reqData) {
        return doBatch(reqData.body());
    }

    @Override
    public Single<JsonObject> discoverThenDoPersist(RequestData reqData) {
        final CommunicationProtocol protocol = parseProtocol(reqData);
        final BACnetNetworkCache cache = networkCache();
        final Optional<UUID> networkId = cache.getDataKey(protocol.identifier());
        if (networkId.isPresent()) {
            throw new AlreadyExistException(
                "Already persisted network code " + protocol.identifier() + " with id " + networkId.get());
        }
        final JsonObject network = JsonPojo.from(new BACnetNetworkTranslator().serialize(protocol)).toJson();
        return doPersist(network).doOnSuccess(response -> cache.addDataKey(protocol, parsePersistResponse(response)));
    }

    @Override
    protected String parseResourceId(@NonNull JsonObject resource) {
        return resource.getString("id");
    }

    //TODO need to split case physical network like ip and transport network like udp. For example: `ipv4-eth0`,
    // `udp4-eth0-47808`, `udp4-eth0-47809`
    private CommunicationProtocol parseProtocol(RequestData reqData) {
        return parseNetworkProtocol(DiscoverRequest.from(reqData, DiscoverLevel.NETWORK));
    }

}
