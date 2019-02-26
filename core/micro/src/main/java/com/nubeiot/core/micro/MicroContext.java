package com.nubeiot.core.micro;

import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.UnitContext;
import com.nubeiot.core.micro.MicroConfig.CircuitBreakerConfig;
import com.nubeiot.core.micro.MicroConfig.ServiceDiscoveryConfig;

import lombok.Getter;

public final class MicroContext extends UnitContext {

    private static final Logger logger = LoggerFactory.getLogger(MicroContext.class);

    private final Set<Record> records = new ConcurrentHashSet<>();
    @Getter
    private ServiceDiscovery discovery;
    @Getter
    private CircuitBreaker circuitBreaker;

    void create(Vertx vertx, MicroConfig config) {
        ServiceDiscoveryConfig discoveryConfig = config.getDiscoveryConfig();
        logger.info("Service Discovery Config : {}", discoveryConfig.toJson().encode());
        discovery = ServiceDiscovery.create(vertx, discoveryConfig);

        CircuitBreakerConfig circuitConfig = config.getCircuitConfig();
        logger.info("Service Discovery Config : {}", circuitConfig.toJson().encode());
        circuitBreaker = CircuitBreaker.create(circuitConfig.getCircuitName(), vertx, circuitConfig.getOptions());
    }

    Iterable<? extends CompletableSource> unregister() {
        return records.stream().map(r -> discovery.rxUnpublish(r.getRegistration())).collect(Collectors.toList());
    }

    public Single<Record> register(Record record) {
        return discovery.rxPublish(record).doOnSuccess(rec -> {
            records.add(rec);
            logger.info("Service <{}> published with {}", rec.getName(), rec.getMetadata());
        });
    }

}
