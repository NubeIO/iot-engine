package com.nubeiot.edge.module.monitor;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.component.ContainerVerticle;
import com.nubeiot.core.component.SharedDataDelegate;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;

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
        EventController controller = SharedDataDelegate.getEventController(vertx.getDelegate(), getSharedKey());
        Observable.fromIterable(MonitorService.createServices())
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(service -> controller.register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(discovery, s))
                  .subscribe();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, MonitorService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
