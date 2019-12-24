package com.nubeiot.core.micro.register;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.utils.ExecutorHelpers;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents for Event http service register.
 *
 * @param <S> Type of {@code EventHttpService}
 * @see EventHttpService
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventHttpServiceRegister<S extends EventHttpService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHttpServiceRegister.class);

    @NonNull
    private final Vertx vertx;
    @NonNull
    private final String sharedKey;
    @NonNull
    private final Supplier<Set<S>> eventServices;

    /**
     * Create event http service register.
     *
     * @param <S>              Type of {@code EventHttpService}
     * @param vertx            the vertx
     * @param sharedKey        the shared key
     * @param servicesProvider the services provider
     * @return the register
     * @since 1.0.0
     */
    public static <S extends EventHttpService> EventHttpServiceRegister<S> create(@NonNull Vertx vertx,
                                                                                  @NonNull String sharedKey,
                                                                                  @NonNull Supplier<Set<S>> servicesProvider) {
        return new EventHttpServiceRegister<S>(vertx, sharedKey, servicesProvider);
    }

    /**
     * Publish services to external API and register event listener by address at the same time.
     *
     * @param discovery the discovery
     * @return list {@code records}
     * @see Record
     * @since 1.0.0
     */
    public Single<List<Record>> publish(@NonNull ServiceDiscoveryController discovery) {
        final EventbusClient client = SharedDataDelegate.getEventController(vertx, sharedKey);
        return ExecutorHelpers.blocking(vertx, eventServices::get)
                              .flattenAsObservable(s -> s)
                              .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                     .ifPresent(service -> client.register(service.address(), service)))
                              .filter(s -> Objects.nonNull(s.definitions()))
                              .flatMap(s -> registerEndpoint(discovery, s))
                              .toList()
                              .doOnSuccess(r -> LOGGER.info("Published {} Service API(s)", r.size()));
    }

    private Observable<Record> registerEndpoint(@NonNull ServiceDiscoveryController discovery, @NonNull S service) {
        if (!discovery.isEnabled()) {
            return Observable.empty();
        }
        return Observable.fromIterable(service.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(service.api(), service.address(), e));
    }

}
