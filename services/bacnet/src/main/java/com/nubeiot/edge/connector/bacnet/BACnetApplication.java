package com.nubeiot.edge.connector.bacnet;

import java.util.Objects;
import java.util.Optional;

import io.github.zero88.qwe.component.ContextLookup;
import io.github.zero88.qwe.component.SharedDataLocalProxy;
import io.github.zero88.qwe.http.event.WebSocketServerEventMetadata;
import io.github.zero88.qwe.http.server.HttpServerProvider;
import io.github.zero88.qwe.http.server.HttpServerRouter;
import io.github.zero88.qwe.micro.MicroContext;
import io.github.zero88.qwe.micro.MicroVerticleProvider;
import io.github.zero88.qwe.micro.ServiceDiscoveryInvoker;
import io.github.zero88.qwe.scheduler.SchedulerProvider;
import io.github.zero88.qwe.storage.json.JsonStorageProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

import com.nubeiot.edge.connector.bacnet.cache.BACnetCacheInitializer;
import com.nubeiot.edge.connector.bacnet.internal.listener.WhoIsListener;
import com.nubeiot.edge.connector.bacnet.service.BACnetApis;
import com.nubeiot.edge.connector.bacnet.service.BACnetApisHelper;
import com.nubeiot.edge.connector.bacnet.service.scheduler.BACnetSchedulerApis;
import com.nubeiot.edge.connector.bacnet.websocket.WebSocketCOVSubscriber;

import lombok.NonNull;

/*
 * BACnet Application
 */
public class BACnetApplication extends AbstractBACnetApplication<BACnetServiceConfig> {

    private MicroContext microContext;

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
        this.microContext = lookup.query(MicroContext.class);
        new BACnetCacheInitializer().init(this);
        super.onInstallCompleted(lookup);
    }

    @Override
    protected @NonNull Class<BACnetServiceConfig> bacnetConfigClass() {
        return BACnetServiceConfig.class;
    }

    @Override
    protected @NonNull Single<JsonObject> registerApis(@NonNull SharedDataLocalProxy sharedData,
                                                       @NonNull BACnetServiceConfig config) {
        final ServiceDiscoveryInvoker invoker = microContext.getLocalInvoker();
        return Observable.fromIterable(BACnetApisHelper.createServices(sharedData))
                         .doOnEach(so -> Optional.ofNullable(so.getValue())
                                                 .ifPresent(s -> getEventbus().register(s.address(), s)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(invoker, s))
                         .map(Record::toJson)
                         .count()
                         .flatMap(l -> BACnetSchedulerApis.registerApis(invoker, sharedData()).map(r -> l + 1))
                         .map(total -> new JsonObject().put("message", "Registered " + total + " BACnet APIs"));
    }

    @Override
    protected void addListenerOnEachDevice(@NonNull BACnetDevice device) {
        device.addListeners(new WhoIsListener());
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
