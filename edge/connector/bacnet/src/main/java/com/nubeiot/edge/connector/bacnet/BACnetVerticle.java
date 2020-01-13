package com.nubeiot.edge.connector.bacnet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.protocol.CommunicationProtocol;
import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.cache.BACnetDeviceCache;
import com.nubeiot.edge.connector.bacnet.handler.BACnetDiscoverFinisher;
import com.nubeiot.edge.connector.bacnet.handler.DiscoverCompletionHandler;
import com.nubeiot.edge.connector.bacnet.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.service.BACnetRpcClient;
import com.nubeiot.edge.connector.bacnet.service.discover.BACnetDiscoveryService;
import com.nubeiot.edge.connector.bacnet.service.rpc.BACnetRpcClientHelper;
import com.nubeiot.edge.connector.bacnet.service.rpc.BACnetSubscription;

import lombok.NonNull;

/*
 * Main BACnetInstance verticle
 */
public final class BACnetVerticle extends AbstractBACnetVerticle<BACnetConfig> {

    private MicroContext microContext;
    private BACnetSubscription subscription;

    @Override
    public void start() {
        super.start();
        this.addProvider(new MicroserviceProvider(), ctx -> microContext = (MicroContext) ctx);
    }

    @Override
    protected void successHandler(@NonNull BACnetConfig config) {
        this.subscription = new BACnetSubscription(getVertx(), getSharedKey(), config.isAllowSlave());
        super.successHandler(new BACnetCacheInitializer(config).init(this).getConfig());
    }

    @Override
    protected @NonNull Class<BACnetConfig> bacnetConfigClass() {
        return BACnetConfig.class;
    }

    @Override
    protected @NonNull Single<JsonObject> registerApis(@NonNull EventbusClient client, @NonNull BACnetConfig config) {
        return Observable.fromIterable(BACnetDiscoveryService.createServices(getVertx(), getSharedKey()))
                         .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                .ifPresent(service -> client.register(service.address(), service)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(microContext.getLocalController(), s))
                         .map(Record::toJson)
                         .count()
                         .map(total -> new JsonObject().put("message", "Registered " + total + " BACnet APIs"))
                         .doOnSuccess(logger::info);
    }

    @Override
    protected @NonNull Single<JsonObject> registerSubscriber(@NonNull EventbusClient client,
                                                             @NonNull BACnetConfig config) {
        return Observable.fromIterable(BACnetRpcClientHelper.createSubscribers(getVertx(), getSharedKey()))
                         .doOnNext(subscriber -> client.register(subscriber.address(), subscriber))
                         .flatMapSingle(subscription::register)
                         .count()
                         .map(total -> new JsonObject().put("message",
                                                            "Registered " + subscription.subscribers().size() + " in " +
                                                            total + " BACnet Subscribers"))
                         .doOnSuccess(logger::info);
    }

    @Override
    protected void addListenerOnEachDevice(@NonNull BACnetDevice device) {
        device.addListeners(new WhoIsListener());
    }

    @Override
    protected @NonNull Single<List<CommunicationProtocol>> availableNetworks(@NonNull BACnetConfig config) {
        return BACnetRpcClientHelper.createScanner(getVertx(), getSharedKey()).scan().onErrorReturn(t -> {
            BACnetRpcClient.sneakyThrowable(logger, t, config.isAllowSlave());
            return Collections.emptyList();
        });
    }

    @Override
    protected Single<JsonObject> stopBACnet() {
        final BACnetDeviceCache deviceCache = SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(),
                                                                                   BACnetCacheInitializer.BACNET_DEVICE_CACHE);
        return subscription.unregisterAll()
                           .flatMap(output -> Observable.fromIterable(deviceCache.all().values())
                                                        .flatMapSingle(BACnetDevice::stop)
                                                        .collect(JsonObject::new, (object, device) -> object.put(
                                                            device.protocol().identifier(), device.metadata().toJson()))
                                                        .map(devices -> new JsonObject().put("terminated", devices))
                                                        .map(json -> json.put("unsubscribed",
                                                                              "Unregistered " + output.size() +
                                                                              " BACnet Subscribers")));
    }

    @Override
    protected DiscoverCompletionHandler createDiscoverCompletionHandler() {
        return new BACnetDiscoverFinisher(s -> SharedDataDelegate.getLocalDataValue(getVertx(), getSharedKey(), s));
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BACnetDiscoveryService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
