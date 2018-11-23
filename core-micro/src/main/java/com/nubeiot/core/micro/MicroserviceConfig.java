package com.nubeiot.core.micro;

import java.util.Set;
import java.util.stream.Collectors;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class MicroserviceConfig {

    private static final Logger logger = LoggerFactory.getLogger(MicroserviceConfig.class);
    public static final String MICRO_CFG_NAME = "__micro__";
    public static final String CIRCUIT_BREAKER_CFG_NAME = "__circuitBreaker__";
    public static final String SERVICE_DISCOVERY_CFG_NAME = "__serviceDiscovery__";

    private final Vertx vertx;
    @Getter
    private final JsonObject microConfig;
    private final Set<Record> registeredRecords = new ConcurrentHashSet<>();
    @Getter
    private ServiceDiscovery discovery;
    @Getter
    private CircuitBreaker circuitBreaker;

    public MicroserviceConfig onStart() {
        logger.info("Setup micro-service...", microConfig);
        JsonObject discoveryCfg = microConfig.getJsonObject(SERVICE_DISCOVERY_CFG_NAME, new JsonObject());
        JsonObject breakerCfg = microConfig.getJsonObject(CIRCUIT_BREAKER_CFG_NAME, new JsonObject());
        ServiceDiscoveryOptions discoveryOptions = new ServiceDiscoveryOptions().setBackendConfiguration(discoveryCfg);
        CircuitBreakerOptions breakerOptions = new CircuitBreakerOptions(breakerCfg);
        logger.debug("Service Discovery Config : {}", discoveryCfg.encode());
        logger.info("Service Discovery Options: {}", discoveryOptions.toJson().encode());
        logger.debug("Circuit Breaker Config : {}", breakerCfg.encode());
        logger.info("Circuit Breaker Options: {}", breakerOptions.toJson().encode());
        discovery = ServiceDiscovery.create(vertx, discoveryOptions);
        circuitBreaker = CircuitBreaker.create(breakerCfg.getString("name", "nubeio-circuit-breaker"), vertx,
                                               breakerOptions);
        return this;
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
