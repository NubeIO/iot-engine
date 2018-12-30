package com.nubeiot.core.micro;

import java.util.Set;
import java.util.stream.Collectors;

import com.nubeiot.core.component.IComponent;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Microservice implements IComponent {

    private final Logger logger = LoggerFactory.getLogger(Microservice.class);
    private final Vertx vertx;
    @Getter
    private final MicroConfig microConfig;
    //TODO: register when creating MICRO_SERVICE
    private final Set<Record> records = new ConcurrentHashSet<>();
    @Getter
    private ServiceDiscovery discovery;
    @Getter
    private CircuitBreaker circuitBreaker;

    public Single<Record> publish(Record record) {
        return discovery.rxPublish(record).doOnSuccess(rec -> {
            records.add(rec);
            logger.info("Service <{}> published with {}", rec.getName(), rec.getMetadata());
        });
    }

    @Override
    public void start() {
        logger.info("Setup micro-service...");
        MicroConfig.ServiceDiscoveryConfig discoveryConfig = microConfig.getDiscoveryConfig();
        logger.info("Service Discovery Config : {}", discoveryConfig.toJson().encode());
        discovery = ServiceDiscovery.create(vertx, discoveryConfig);

        MicroConfig.CircuitBreakerConfig circuitConfig = microConfig.getCircuitConfig();
        logger.info("Service Discovery Config : {}", circuitConfig.toJson().encode());
        circuitBreaker = CircuitBreaker.create(circuitConfig.getCircuitName(), vertx, circuitConfig.getOptions());
    }

    @Override
    public void stop() {

    }

    @Override
    public void stop(Future<Void> future) {
        Iterable<CompletableSource> unPublishSources = records.stream()
                                                              .map(r -> discovery.rxUnpublish(r.getRegistration()))
                                                              .collect(Collectors.toList());
        Completable.merge(unPublishSources).doOnComplete(future::complete).doOnError(future::fail).subscribe();
    }

}
