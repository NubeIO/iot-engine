package com.nubeiot.core.micro;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.http.client.ClientUtils;
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

    abstract <T extends ServiceGatewayAnnounceMonitor> void subscribe(EventBus eventBus, @NonNull T announceMonitor);

    abstract <T extends ServiceGatewayUsageMonitor> void subscribe(EventBus eventBus, @NonNull T usageMonitor);

    abstract String kind();

    abstract String computeINet(String host);

    final void subscribe(io.vertx.core.Vertx vertx, String announceMonitorClass, String usageMonitorClass) {
        subscribe(vertx.eventBus(), ServiceGatewayAnnounceMonitor.create(vertx, this, sharedKey, announceMonitorClass));
        subscribe(vertx.eventBus(), ServiceGatewayUsageMonitor.create(vertx, this, sharedKey, usageMonitorClass));
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
            serviceDiscovery.rxGetRecords(r -> true, true)
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

    public Single<Record> addEventMessageRecord(String name, String address, EventMethodDefinition definition,
                                                JsonObject metadata) {
        return addDecoratorRecord(EventMessageService.createRecord(name, address, definition, metadata));
    }

    /**
     * Will be removed soon
     *
     * @deprecated Use {@link #executeHttpService(Function, String, HttpMethod, RequestData)}
     */
    public Single<Buffer> executeHttpService(Function<Record, Boolean> filter, String path, HttpMethod method,
                                             JsonObject headers, JsonObject payload) {
        return findRecord(filter, HttpEndpoint.TYPE).flatMap(record -> {
            ServiceReference reference = get().getReference(record);
            return circuitController.wrap(
                ClientUtils.execute(reference.getAs(io.vertx.reactivex.core.http.HttpClient.class), path, method,
                                    headers, payload, v -> reference.release()));
        }).doOnError(t -> logger.error("Failed when redirect to {}::{}", t, method, path));
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
            HttpClientDelegate delegate = HttpClientDelegate.create(reference.getAs(HttpClient.class))
                                                            .overrideEndHandler(v -> reference.release());
            return circuitController.wrap(delegate.execute(path, method, requestData, false))
                                    .doFinally(reference::release);
        }).doOnError(t -> logger.error("Failed when redirect to {}::{}", t, method, path));
    }

    public Single<ResponseData> executeEventMessageService(Function<Record, Boolean> filter, String path,
                                                           HttpMethod method, RequestData requestData) {
        return executeEventMessageService(filter, path, method, requestData, null);
    }

    public Single<ResponseData> executeEventMessageService(Function<Record, Boolean> filter, String path,
                                                           HttpMethod method, RequestData requestData,
                                                           DeliveryOptions options) {
        return findRecord(filter, EventMessageService.TYPE).flatMap(record -> {
            JsonObject config = new JsonObject().put(EventMessageService.SHARED_KEY_CONFIG, this.sharedKey)
                                                .put(EventMessageService.DELIVERY_OPTIONS_CONFIG,
                                                     Objects.isNull(options) ? new JsonObject() : options.toJson());
            ServiceReference reference = get().getReferenceWithConfiguration(record, config);
            return circuitController.wrap(execute(reference.getAs(EventMessagePusher.class), path, method, requestData,
                                                  v -> reference.release()));
        }).doOnError(t -> logger.error("Failed when redirect to {} :: {}", t, method, path));
    }

    private Single<Record> findRecord(Function<Record, Boolean> filter, String type) {
        return get().rxGetRecord(r -> type.equals(r.getType()) && filter.apply(r))
                    .switchIfEmpty(Single.error(
                        new ServiceException("Service Unavailable", new NotFoundException("Not found " + type))));
    }

    private Single<Record> addDecoratorRecord(@NonNull Record record) {
        return get().rxPublish(record)
                    .doOnSuccess(rec -> logger.info("Published {} Service: {}", kind(), rec.toJson()))
                    .doOnError(t -> logger.error("Cannot publish {} record: {}", t, kind(), record));
    }

    private Record decorator(Record record) {
        if (!HttpEndpoint.TYPE.equals(record.getType())) {
            return record;
        }
        HttpLocation location = new HttpLocation(record.getLocation());
        location.setHost(computeINet(location.getHost()));
        return record.setLocation(location.toJson());
    }

    private Single<ResponseData> execute(EventMessagePusher pusher, String path, HttpMethod method,
                                         RequestData requestData, Handler<Void> closeHandler) {

        return Single.<ResponseData>create(
            source -> pusher.push(path, method, requestData, source::onSuccess, source::onError)).doFinally(
            () -> closeHandler.handle(null));
    }

}
