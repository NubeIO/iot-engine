package com.nubeiot.core.micro.register;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import io.reactivex.Observable;
import io.vertx.core.Vertx;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.http.base.EventHttpService;
import com.nubeiot.core.micro.ServiceDiscoveryController;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class EventHttpServiceRegister<T extends EventHttpService> {

    @NonNull
    private final Vertx vertx;
    @NonNull
    private final String sharedKey;
    @NonNull
    private final Set<T> eventServices;

    public Observable<Record> publish(@NonNull ServiceDiscoveryController discovery) {
        final EventController client = SharedDataDelegate.getEventController(vertx, sharedKey);
        return Observable.fromIterable(eventServices)
                         .doOnEach(s -> Optional.ofNullable(s.getValue())
                                                .ifPresent(service -> client.register(service.address(), service)))
                         .filter(s -> Objects.nonNull(s.definitions()))
                         .flatMap(s -> registerEndpoint(discovery, s));
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, T s) {
        if (!discovery.isEnabled()) {
            return Observable.empty();
        }
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
