package com.nubeiot.core.micro;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;

import com.nubeiot.core.dto.RequestData;
import com.nubeiot.core.dto.ResponseData;
import com.nubeiot.core.exceptions.HttpException;
import com.nubeiot.core.exceptions.NotFoundException;
import com.nubeiot.core.exceptions.ServiceException;
import com.nubeiot.core.micro.MicroConfig.BackendConfig;
import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;
import com.nubeiot.core.utils.Networks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ServiceDiscoveryController implements Supplier<ServiceDiscovery> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryController.class);

    @Getter(value = AccessLevel.PACKAGE)
    protected final ServiceDiscoveryConfig config;
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

    abstract String kind();

    abstract void subscribe(EventBus eventBus, String announceMonitorClass, String usageMonitorClass);

    @Override
    public ServiceDiscovery get() {
        return Objects.requireNonNull(this.serviceDiscovery, kind() + " Service Discovery is not enabled");
    }

    void unregister(Future future) {
        if (Objects.nonNull(serviceDiscovery)) {
            serviceDiscovery.rxGetRecords(r -> true, true)
                            .flattenAsObservable(rs -> rs)
                            .flatMapCompletable(r -> serviceDiscovery.rxUnpublish(r.getRegistration()))
                            .subscribe(future::succeeded, future::fail);
        }
    }

    public Single<Record> addRecord(@NonNull Record record) {
        return addDecoratorRecord(decorator(record));
    }

    public Single<Record> addHttpRecord(String name, HttpLocation httpLocation, JsonObject metadata) {
        Record record = HttpEndpoint.createRecord(name, httpLocation.isSsl(),
                                                  Networks.computeNATAddress(httpLocation.getHost()),
                                                  httpLocation.getPort(), httpLocation.getRoot(), metadata);
        return addDecoratorRecord(record);
    }

    public Single<Buffer> executeHttpService(Function<Record, Boolean> filter, String path, HttpMethod method,
                                             JsonObject headers, JsonObject payload) {
        return get().rxGetRecord(r -> HttpEndpoint.TYPE.equals(r.getType()) && filter.apply(r))
                    .switchIfEmpty(Single.error(
                        new ServiceException("Service Unavailable", new NotFoundException("Not found HTTP endpoint"))))
                    .flatMap(record -> {
                        ServiceReference reference = get().getReference(record);
                        return circuitController.wrap(
                            execute(reference.getAs(HttpClient.class), path, method, headers, payload,
                                    v -> reference.release()));
                    })
                    .doOnError(t -> logger.error("Failed when redirect to {} :: {}", t, method, path));
    }

    public Single<ResponseData> executeHttpService(Function<Record, Boolean> filter, String path, HttpMethod method,
                                                   RequestData requestData) {
        return get().rxGetRecord(r -> HttpEndpoint.TYPE.equals(r.getType()) && filter.apply(r))
                    .switchIfEmpty(Single.error(
                        new ServiceException("Service Unavailable", new NotFoundException("Not found HTTP endpoint"))))
                    .flatMap(record -> {
                        ServiceReference reference = get().getReference(record);
                        return circuitController.wrap(
                            execute(reference.getAs(HttpClient.class), path, method, requestData,
                                    v -> reference.release()));
                    })
                    .doOnError(t -> logger.error("Failed when redirect to {} :: {}", t, method, path));
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
        location.setHost(Networks.computeNATAddress(location.getHost()));
        return record.setLocation(location.toJson());
    }

    //TODO move into HTTPClient
    private Single<Buffer> execute(HttpClient httpClient, String path, HttpMethod method, JsonObject headers,
                                   JsonObject payload, Handler<Void> closeHandler) {
        return Single.create(source -> {
            HttpClientRequest request = httpClient.request(method, path, response -> response.bodyHandler(body -> {
                logger.debug("Response status {}", response.statusCode());
                if (response.statusCode() >= 400) {
                    source.onError(new HttpException(response.statusCode(), body.toString()));
                    logger.warn("Failed to execute: {}", response.toString());
                } else {
                    source.onSuccess(body);
                }
            })).endHandler(v -> {
                httpClient.close();
                closeHandler.handle(v);
            });
            logger.info("Make HTTP request {} :: {} | <{}> | <{}>", request.method(), request.absoluteURI(), headers,
                        payload);
            //TODO why need it?
            request.setChunked(true);
            for (String header : headers.fieldNames()) {
                request.putHeader(header, headers.getValue(header).toString());
            }
            if (payload == null) {
                request.end();
            } else {
                request.write(payload.encode()).end();
            }
        });
    }

    //TODO move into HTTPClient
    private Single<ResponseData> execute(HttpClient httpClient, String path, HttpMethod method, RequestData requestData,
                                         Handler<Void> closeHandler) {
        return Single.create(source -> {
            HttpClientRequest request = httpClient.request(method, path, response -> response.bodyHandler(body -> {
                logger.debug("Response status {}", response.statusCode());
                if (response.statusCode() >= 400) {
                    source.onError(new HttpException(response.statusCode(), body.toString()));
                    logger.warn("Failed to execute: {}", response.toString());
                } else {
                    source.onSuccess(new ResponseData().setBody(body.toJsonObject()));
                }
            })).endHandler(v -> {
                httpClient.close();
                closeHandler.handle(v);
            });
            logger.info("Make HTTP request {} :: {} | <{}> | <{}>", request.method(), request.absoluteURI(),
                        requestData.toJson());
            //TODO why need it?
            request.setChunked(true);
            for (String header : requestData.headers().fieldNames()) {
                request.putHeader(header, requestData.headers().getValue(header).toString());
            }
            if (requestData.body() == null) {
                request.end();
            } else {
                request.write(requestData.body().encode()).end();
            }
        });
    }

}
