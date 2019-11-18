package com.nubeiot.edge.module.monitor;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.core.utils.ExecutorHelpers;
import com.nubeiot.edge.module.monitor.service.MonitorService;

public final class MonitorVerticle extends ContainerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishServices);
    }

    private void publishServices(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        ExecutorHelpers.blocking(vertx.getDelegate(), MonitorService::createServices)
                       .flattenAsObservable(s -> s)
                       .doOnEach(s -> Optional.ofNullable(s.getValue())
                                              .ifPresent(
                                                  service -> getEventbusClient().register(service.address(), service)))
                       .filter(s -> Objects.nonNull(s.definitions()))
                       .flatMap(s -> registerEndpoint(discovery, s))
                       .subscribe();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, MonitorService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
