package com.nubeiot.edge.connector.bacnet;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import io.github.zero88.qwe.component.ContextLookup;
import io.github.zero88.qwe.event.EventbusClient;
import io.github.zero88.qwe.http.event.WebSocketServerEventMetadata;
import io.github.zero88.qwe.http.server.HttpServerProvider;
import io.github.zero88.qwe.http.server.HttpServerRouter;
import io.github.zero88.qwe.micro.MicroContext;
import io.github.zero88.qwe.micro.MicroVerticleProvider;
import io.github.zero88.qwe.micro.ServiceDiscoveryInvoker;
import io.github.zero88.qwe.protocol.CommunicationProtocol;
import io.github.zero88.qwe.scheduler.SchedulerProvider;
import io.github.zero88.qwe.storage.json.JsonStorageProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.cache.BACnetNetworkCache;
import com.nubeiot.edge.connector.bacnet.handler.BACnetDiscoverFinisher;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.internal.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetApisHelper;
import com.nubeiot.edge.connector.bacnet.service.scheduler.BACnetSchedulerApis;
import com.nubeiot.edge.connector.bacnet.service.subscriber.BACnetRpcClientHelper;
import com.nubeiot.edge.connector.bacnet.service.subscriber.BACnetSubscriptionManager;
import com.nubeiot.edge.connector.bacnet.websocket.WebSocketCOVSubscriber;

import lombok.NonNull;

/*
 * BACnet Application
 */
public class BACnetApplication extends AbstractBACnetApplication<BACnetServiceConfig> {

    private MicroContext microContext;
    private BACnetSubscriptionManager manager;

    @Override
    public String configFile() {
        return "bacnet.json";
    }

    @Override
    public void start() {
        super.start();
        this.addProvider(new HttpServerProvider(initRouter()))
            .addProvider(new MicroVerticleProvider())
            .addProvider(new SchedulerProvider())
            .addProvider(new JsonStorageProvider());
    }

    @Override
    public void onInstallCompleted(@NonNull ContextLookup lookup) {
        this.manager = new BACnetSubscriptionManager(getVertx(), getSharedKey());
        this.microContext = lookup.query(MicroContext.class);
        new BACnetCacheInitializer().init(this);
        super.onInstallCompleted(lookup);
    }

    @Override
    protected @NonNull Class<BACnetServiceConfig> bacnetConfigClass() {
        return BACnetServiceConfig.class;
    }

    @Override
    protected @NonNull Single<JsonObject> registerApis(@NonNull EventbusClient client,
                                                       @NonNull BACnetServiceConfig config) {
        final ServiceDiscoveryInvoker invoker = microContext.getLocalInvoker();
        return Observable.fromIterable(BACnetApisHelper.createServices(this))
                         .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                .ifPresent(service -> client.register(service.address(), service)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(invoker, s))
                         .map(Record::toJson)
                         .count()
                         .flatMap(l -> BACnetSchedulerApis.registerApis(invoker, sharedData()).map(r -> l + 1))
                         .map(total -> new JsonObject().put("message", "Registered " + total + " BACnet APIs"));
    }

    @Override
    protected @NonNull Single<JsonObject> registerSubscriber(@NonNull EventbusClient client,
                                                             @NonNull BACnetServiceConfig config) {
        return Observable.fromIterable(BACnetRpcClientHelper.createSubscribers(getVertx(), getSharedKey()))
                         .doOnNext(subscriber -> client.register(subscriber.address(), subscriber))
                         .flatMapSingle(manager::register)
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
    protected @NonNull Single<Collection<CommunicationProtocol>> availableNetworks(
        @NonNull BACnetServiceConfig config) {
        final BACnetNetworkCache cache = this.sharedData().getData(BACnetCacheInitializer.LOCAL_NETWORK_CACHE);
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
        final BACnetDeviceCache deviceCache = this.sharedData().getData(BACnetCacheInitializer.BACNET_DEVICE_CACHE);
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
        return new BACnetDiscoverFinisher(this);
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryInvoker discovery, BACnetApis s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

    private HttpServerRouter initRouter() {
        final WebSocketCOVSubscriber subscriber = WebSocketCOVSubscriber.builder().build();
        return new HttpServerRouter().registerEventBusSocket(
            WebSocketServerEventMetadata.create(subscriber.getWsPath(), subscriber.toEventModel()));
    }

}
