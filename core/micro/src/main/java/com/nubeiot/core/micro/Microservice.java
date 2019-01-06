package com.nubeiot.core.micro;

import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.UnitVerticle;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Microservice extends UnitVerticle<MicroConfig> {

    //TODO: register when creating MICRO_SERVICE
    private final Set<Record> records = new ConcurrentHashSet<>();
    @Getter
    private ServiceDiscovery discovery;
    @Getter
    private CircuitBreaker circuitBreaker;

    @Override
    public void start() {
        super.start();
        logger.info("Setup micro-service...");
        MicroConfig.ServiceDiscoveryConfig discoveryConfig = config.getDiscoveryConfig();
        logger.info("Service Discovery Config : {}", discoveryConfig.toJson().encode());
        discovery = ServiceDiscovery.create(Vertx.newInstance(vertx), discoveryConfig);

        MicroConfig.CircuitBreakerConfig circuitConfig = config.getCircuitConfig();
        logger.info("Service Discovery Config : {}", circuitConfig.toJson().encode());
        circuitBreaker = CircuitBreaker.create(circuitConfig.getCircuitName(), Vertx.newInstance(vertx),
                                               circuitConfig.getOptions());
    }

    @Override
    public void stop(Future<Void> future) {
        Iterable<CompletableSource> unPublishSources = records.stream()
                                                              .map(r -> discovery.rxUnpublish(r.getRegistration()))
                                                              .collect(Collectors.toList());
        Completable.merge(unPublishSources).doOnComplete(future::complete).doOnError(future::fail).subscribe();
    }

    @Override
    public Class<MicroConfig> configClass() {
        return MicroConfig.class;
    }

    @Override
    public String configFile() { return "micro.json"; }

    public Single<Record> publish(Record record) {
        return discovery.rxPublish(record).doOnSuccess(rec -> {
            records.add(rec);
            logger.info("Service <{}> published with {}", rec.getName(), rec.getMetadata());
        });
    }

}
