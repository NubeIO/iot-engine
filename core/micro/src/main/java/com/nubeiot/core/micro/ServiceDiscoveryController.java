package com.nubeiot.core.micro;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Status;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.client.HttpClientDelegate;
import com.nubeiot.core.micro.MicroConfig.BackendConfig;
import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;
import com.nubeiot.core.micro.type.EventMessagePusher;
import com.nubeiot.core.micro.type.EventMessageService;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ServiceDiscoveryController implements Supplier<ServiceDiscovery> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryController.class);
    @Getter(value = AccessLevel.PACKAGE)
    protected final ServiceDiscoveryConfig config;
    private final String sharedKey;
    private final ServiceDiscovery serviceDiscovery;
    private final CircuitBreakerController circuitController;
    private final Map<String, Record> registrationMap = new ConcurrentHashMap<>();

    static ServiceDiscovery createServiceDiscovery(Vertx vertx, ServiceDiscoveryConfig config, String kind,
                                                   Predicate<Vertx> predicate) {
        if (!config.isEnabled() || !predicate.test(vertx)) {
            logger.info("Skip setup {} Service Discovery", kind);
            return null;
        }
        logger.info("{} Service Discovery Config : {}", kind, config.toJson().encode());
        config.reloadProperty();
        logger.debug("{} Service Discovery | {} | {}", kind, BackendConfig.DEFAULT_SERVICE_DISCOVERY_BACKEND,
                     System.getProperty(BackendConfig.DEFAULT_SERVICE_DISCOVERY_BACKEND));
        return ServiceDiscovery.create(vertx, config);
    }

    public static Function<Record, Boolean> defaultHttpEndpointFilter(String serviceName,
                                                                      @NonNull HttpLocation location) {
        return r -> r.getType().equals(HttpEndpoint.TYPE) &&
                    location.getHost().equals(r.getLocation().getString("host")) &&
                    location.getPort() == r.getLocation().getInteger("port") &&
                    location.getRoot().equals(r.getLocation().getString("root")) && r.getName().equals(serviceName);
    }

    abstract <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, @NonNull T announceMonitor);

    abstract <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor);

    abstract String kind();

    abstract String computeINet(String host);

    final void subscribe(Vertx vertx, String announceMonitorClass, String usageMonitorClass) {
        subscribe(vertx.eventBus(), ServiceGatewayAnnounceMonitor.create(vertx, this, sharedKey, announceMonitorClass));
        subscribe(vertx.eventBus(), ServiceGatewayUsageMonitor.create(vertx, this, sharedKey, usageMonitorClass));
    }

    // TODO: find better way instead force rescan in every register call
    final void rescanService(EventBus eventBus) {
        eventBus.send(config.getAnnounceAddress(), new JsonObject().put("status", Status.UNKNOWN));
    }

    public boolean isEnabled() {
        return this.config.isEnabled();
    }

    @Override
    public ServiceDiscovery get() {
        return Objects.requireNonNull(this.serviceDiscovery, kind() + " Service Discovery is not enabled");
    }

    void unregister(Future future) {
        if (Objects.nonNull(serviceDiscovery)) {
            io.vertx.reactivex.servicediscovery.ServiceDiscovery serviceDiscovery = getRx();
            serviceDiscovery.rxGetRecords(r -> registrationMap.keySet().contains(r.getRegistration()), true)
                            .flattenAsObservable(rs -> rs)
                            .flatMapCompletable(r -> serviceDiscovery.rxUnpublish(r.getRegistration()))
                            .subscribe(future::complete, err -> {
                                logger.warn("Cannot un-published record", err);
                                future.complete();
                            });
        }
    }

    public Single<Record> addRecord(@NonNull Record record) {
        return addDecoratorRecord(decorator(record));
    }

    public Single<Record> addHttpRecord(String name, HttpLocation location, JsonObject metadata) {
        Record record = HttpEndpoint.createRecord(name, location.isSsl(), computeINet(location.getHost()),
                                                  location.getPort(), location.getRoot(), metadata);
        return addDecoratorRecord(record);
    }

    public Single<Record> addEventMessageRecord(String name, String address, EventMethodDefinition definition) {
        return addDecoratorRecord(EventMessageService.createRecord(name, address, definition));
    }

    public Single<Record> addEventMessageRecord(String name, String address, EventMethodDefinition definition,
                                                JsonObject metadata) {
        return addDecoratorRecord(EventMessageService.createRecord(name, address, definition, metadata));
    }

    public Single<ResponseData> executeHttpService(Function<Record, Boolean> filter, String path, HttpMethod method,
                                                   RequestData requestData) {
        return executeHttpService(filter, path, method, requestData, null);
    }

    public Single<ResponseData> executeHttpService(Function<Record, Boolean> filter, String path, HttpMethod method,
                                                   RequestData requestData, HttpClientOptions options) {
        return findRecord(filter, HttpEndpoint.TYPE).flatMap(record -> {
            ServiceReference reference = get().getReferenceWithConfiguration(record, Objects.isNull(options)
                                                                                     ? null
                                                                                     : options.toJson());
            HttpClientDelegate delegate = HttpClientDelegate.create(reference.getAs(HttpClient.class));
            return circuitController.wrap(delegate.execute(path, method, requestData, false))
                                    .doFinally(reference::release);
        }).doOnError(t -> logger.error("Failed when redirect to {}::{}", t, method, path));
    }

    public Single<ResponseData> executeEventMessageService(Function<Record, Boolean> filter, String path,
                                                           HttpMethod method, RequestData requestData) {
        return executeEventMessageService(filter, path, method, requestData.toJson());
    }

    public Single<ResponseData> executeEventMessageService(Function<Record, Boolean> filter, String path,
                                                           HttpMethod method, JsonObject requestData) {
        return executeEventMessageService(filter, path, method, requestData, null);
    }

    public Single<ResponseData> executeEventMessageService(Function<Record, Boolean> filter, String path,
                                                           HttpMethod method, JsonObject requestData,
                                                           DeliveryOptions options) {
        return findRecord(filter, EventMessageService.TYPE).flatMap(record -> {
            JsonObject config = new JsonObject().put(EventMessageService.SHARED_KEY_CONFIG, this.sharedKey)
                                                .put(EventMessageService.DELIVERY_OPTIONS_CONFIG,
                                                     Objects.isNull(options) ? new JsonObject() : options.toJson());
            ServiceReference ref = get().getReferenceWithConfiguration(record, config);
            Single<ResponseData> command = Single.create(source -> ref.getAs(EventMessagePusher.class)
                                                                      .execute(path, method, requestData,
                                                                               source::onSuccess, source::onError));
            return circuitController.wrap(command).doFinally(ref::release);
        }).doOnError(t -> logger.error("Failed when redirect to {} :: {}", t, method, path));
    }

    public Single<Record> findRecord(Function<Record, Boolean> filter, String type) {
        return getRx().rxGetRecord(r -> type.equals(r.getType()) && filter.apply(r))
                      .switchIfEmpty(Single.error(
                          new ServiceException("Service Unavailable", new NotFoundException("Not found " + type))));
    }

    public Single<Boolean> contains(Function<Record, Boolean> filter, String type) {
        return getRx().rxGetRecord(r -> type.equals(r.getType()) && filter.apply(r)).count().map(c -> c > 0);
    }

    public Maybe<Record> get(@NonNull Function<Record, Boolean> filter) {
        return getRx().rxGetRecord(filter);
    }

    public Single<List<Record>> getRecords() {
        return getRx().rxGetRecords(r -> true, true);
    }

    public Completable removeRecord(String registration) {
        return getRx().rxGetRecord(r -> r.getRegistration().equals(registration), true)
                      .switchIfEmpty(Single.error(new NotFoundException("Not found that registration")))
                      .flatMapCompletable(record -> {
                          registrationMap.remove(registration);
                          return getRx().rxUnpublish(registration);
                      });
    }

    private Single<Record> addDecoratorRecord(@NonNull Record record) {
        return getRx().rxPublish(record).doOnSuccess(rec -> {
            registrationMap.put(rec.getRegistration(), rec);
            logger.info("Published {} Service | Registration: {} | API: {} | Type: {} | Endpoint: {}", kind(),
                        rec.getRegistration(), rec.getName(), rec.getType(), rec.getLocation().getString("endpoint"));
            if (logger.isDebugEnabled()) {
                logger.debug("Published {} Service: {}", kind(), rec.toJson());
            }
        }).doOnError(t -> logger.error("Cannot publish {} record: {}", t, kind(), record.toJson()));
    }

    private Record decorator(Record record) {
        if (!HttpEndpoint.TYPE.equals(record.getType())) {
            return record;
        }
        HttpLocation location = new HttpLocation(record.getLocation());
        location.setHost(computeINet(location.getHost()));
        return record.setLocation(location.toJson());
    }

    private io.vertx.reactivex.servicediscovery.ServiceDiscovery getRx() {
        return io.vertx.reactivex.servicediscovery.ServiceDiscovery.newInstance(get());
    }

}
