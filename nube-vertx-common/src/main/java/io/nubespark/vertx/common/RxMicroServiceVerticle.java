package io.nubespark.vertx.common;


import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.reactivex.servicediscovery.types.HttpEndpoint;
import io.vertx.reactivex.servicediscovery.types.MessageSource;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Verticle} taking care of the discovery and publication of services.
 */
public abstract class RxMicroServiceVerticle extends AbstractVerticle {

    protected ServiceDiscovery discovery;
    protected CircuitBreaker circuitBreaker;
    private Set<Record> registeredRecords = new ConcurrentHashSet<>();


    public void start() {
        // initializing service discovery
        discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

        // init circuit breaker instance
        JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
                config().getJsonObject("circuit-breaker") : new JsonObject();
        circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
                new CircuitBreakerOptions()
                        .setMaxFailures(cbOptions.getInteger("max-failures", 5))
                        .setTimeout(cbOptions.getLong("timeout", 20000L))
                        .setFallbackOnFailure(true)
                        .setResetTimeout(cbOptions.getLong("reset-timeout", 30000L))
        );
    }

    protected final Single<Record> publishHttpEndpoint(String name, String host, int port) {
        Record record = HttpEndpoint.createRecord(name, host, port, "/",
                new JsonObject().put("api.name", config().getString("api.name", ""))
        );
        return publish(record);
    }


    protected final Single<Record> publishMessageSource(String name, String address) {
        Record record = MessageSource.createRecord(name, address);
        return publish(record);
    }


    private Single<Record> publish(Record record) {
        return discovery.rxPublish(record)
                .doOnSuccess(rec -> {
                    registeredRecords.add(rec);
                    getLogger().info("Service <" + rec.getName() + "> published with " + rec.getMetadata());
                });
    }

    @Override
    public void stop(Future<Void> future) {
        final Iterable<CompletableSource> unPublishSources = registeredRecords.stream()
                .map(record -> discovery.rxUnpublish(record.getRegistration()))
                .collect(Collectors.toList());
        Completable.merge(unPublishSources)
                .doOnComplete(future::complete)
                .doOnError(future::fail)
                .subscribe();
    }

    @NonNull
    protected abstract Logger getLogger();
}
