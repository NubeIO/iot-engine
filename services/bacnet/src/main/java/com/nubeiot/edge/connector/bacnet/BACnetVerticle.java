package com.nubeiot.edge.connector.bacnet;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.github.zero88.qwe.component.SharedDataDelegate;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.http.server.HttpServerProvider;
import io.github.zero88.qwe.http.server.HttpServerRouter;
import io.github.zero88.qwe.micro.MicroContext;
import io.github.zero88.qwe.micro.MicroserviceProvider;
import io.github.zero88.qwe.micro.ServiceDiscoveryController;
import io.github.zero88.qwe.micro.metadata.EventMethodDefinition;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.handler.BACnetDiscoverFinisher;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.service.discover.BACnetRpcDiscoveryService;
import com.nubeiot.edge.connector.bacnet.service.subscriber.BACnetRpcClientHelper;
import com.nubeiot.edge.connector.bacnet.service.subscriber.BACnetRpcSubscription;

import lombok.NonNull;

/*
 * Main BACnetInstance verticle
 */
public final class BACnetVerticle extends AbstractBACnetVerticle<BACnetConfig> {

    private MicroContext microContext;
    private BACnetRpcSubscription subscription;

    @Override
    public String configFile() {
        return "bacnet.json";
    }

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(new HttpServerRouter()))
            .addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx);
    }

    @Override
    protected void successHandler(@NonNull BACnetConfig config) {
        this.subscription = new BACnetRpcSubscription(getVertx(), getSharedKey(), config.isAllowSlave());
        super.successHandler(new BACnetCacheInitializer(config).init(this).getConfig());
    }

    @Override
    protected @NonNull Class<BACnetConfig> bacnetConfigClass() {
        return BACnetConfig.class;
    }

    @Override
    protected @NonNull Single<JsonObject> registerApis(@NonNull EventbusClient client, @NonNull BACnetConfig config) {
        return Observable.fromIterable(BACnetRpcDiscoveryService.createServices(getVertx(), getSharedKey()))
                         .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                .ifPresent(service -> client.register(service.address(), service)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(microContext.getLocalController(), s))
                         .map(Record::toJson)
                         .count()
                         .map(total -> new JsonObject().put("message", "Registered " + total + " BACnet APIs"));
    }

    @Override
    protected @NonNull Single<JsonObject> registerSubscriber(@NonNull EventbusClient client,
                                                             @NonNull BACnetConfig config) {
        return Observable.fromIterable(BACnetRpcClientHelper.createSubscribers(getVertx(), getSharedKey()))
                         .doOnNext(subscriber -> client.register(subscriber.address(), subscriber))
                         .flatMapSingle(subscription::register)
                         .count()
                         .map(total -> new JsonObject().put("message",
                                                            "Registered " + 0/*subscription.subscribers().size()*/ +
                                                            " in " + total + " BACnet Subscribers"));
    }

    @Override
    protected void addListenerOnEachDevice(@NonNull BACnetDevice device) {
        device.addListeners(new WhoIsListener());
    }

    @Override
    protected @NonNull Single<Collection<CommunicationProtocol>> availableNetworks(@NonNull BACnetConfig config) {
        final BACnetNetworkCache cache = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                              BACnetCacheInitializer.EDGE_NETWORK_CACHE);
        return Single.just(Collections.emptyList());
        //        return BACnetScannerHelper.createNetworkScanner(getVertx(), getSharedKey())
        //                                  .scan()
        //                                  .doOnSuccess(map -> map.forEach((key, value) -> cache.addDataKey(value,
        //                                  key)))
        //                                  .map(Map::values)
        //                                  .onErrorReturn(t -> {
        //                                      BACnetRpcProtocol.sneakyThrowable(logger, t, config.isAllowSlave());
        //                                      return Collections.emptyList();
        //                                  });
    }

    @Override
    protected Single<JsonObject> stopBACnet() {
        final BACnetDeviceCache deviceCache = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                                   BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        return Single.just(new JsonObject());
        //        return subscription.unregisterAll()
        //                           .flatMap(output -> Observable.fromIterable(deviceCache.all().values())
        //                                                        .flatMapSingle(BACnetDevice::stop)
        //                                                        .collect(JsonObject::new, (object, device) ->
        //                                                        object.put(
        //                                                            device.protocol().identifier(), device.metadata
        //                                                            ().toJson()))
        //                                                        .map(devices -> new JsonObject().put("terminated",
        //                                                        devices))
        //                                                        .map(json -> json.put("unsubscribed",
        //                                                                              "Unregistered " + output.size
        //                                                                              () +
        //                                                                              " BACnet Subscribers")));
    }

    @Override
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new BACnetDiscoverFinisher(getVertx(), getSharedKey());
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BACnetRpcDiscoveryService s) {
        return Observable.fromIterable((Set<EventMethodDefinition>) s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
