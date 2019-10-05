package com.nubeiot.edge.bios;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;

public class EdgeBiosVerticle extends InstallerVerticle {

    static final String SHARED_MODULE_RULE = "MODULE_RULE";

    @Override
    public void start() {
        super.start();
        this.addSharedData(SHARED_MODULE_RULE, this.getModuleRule())
            .addProvider(new MicroserviceProvider(), this::publishService);
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new EdgeBiosRuleProvider();
    }

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    protected @NonNull EventModel deploymentEvent() {
        return InstallerEventModel.BIOS_DEPLOYMENT;
    }

    @Override
    protected @NonNull EventModel postDeploymentEvent() {
        return InstallerEventModel.BIOS_POST_DEPLOYMENT;
    }

    protected void publishService(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        final EventController client = getEventController();
        Observable.fromIterable(BiosInstallerService.createServices(this))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(service -> client.register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(discovery, s))
                  .subscribe();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BiosInstallerService s) {
        if (!discovery.isEnabled()) {
            return Observable.empty();
        }
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
