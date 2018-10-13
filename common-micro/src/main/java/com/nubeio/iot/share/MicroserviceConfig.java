package com.nubeio.iot.share;

import java.util.Set;
import java.util.stream.Collectors;

import com.nubeio.iot.share.utils.Configs;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MicroserviceConfig {

    private static final Logger logger = LoggerFactory.getLogger(MicroserviceConfig.class);

    private final Vertx vertx;
    private final JsonObject config;
    private final Set<Record> registeredRecords = new ConcurrentHashSet<>();
    @Getter
    private ServiceDiscovery discovery;
    @Getter
    private CircuitBreaker circuitBreaker;

    public MicroserviceConfig onStart() {
        final JsonObject config = Configs.loadDefaultConfig("micro.json").mergeIn(this.config, true);
        clusterMicroService(config);
        return this;
    }

    private void clusterMicroService(JsonObject config) {
        logger.info("Setup cluster with config {}", config);
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config));
        JsonObject cfg = config.getJsonObject("system", new JsonObject())
                               .getJsonObject("circuitBreaker", new JsonObject());
        logger.info("Circuit Breaker Options {}", cfg);
        circuitBreaker = CircuitBreaker.create(cfg.getString("name"), vertx, new CircuitBreakerOptions(cfg));
    }

    public Single<Record> publish(Record record) {
        return discovery.rxPublish(record).doOnSuccess(rec -> {
            registeredRecords.add(rec);
            logger.info("Service <{}> published with {}", rec.getName(), rec.getMetadata());
        });
    }

    public void onStop(Future<Void> future) {
        final Iterable<CompletableSource> unPublishSources = registeredRecords.stream()
                                                                              .map(record -> discovery.rxUnpublish(
                                                                                      record.getRegistration()))
                                                                              .collect(Collectors.toList());
        Completable.merge(unPublishSources).doOnComplete(future::complete).doOnError(future::fail).subscribe();
    }

}
