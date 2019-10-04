package com.nubeiot.edge.bios;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.EdgeVerticle;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public class EdgeBiosVerticle extends EdgeVerticle {

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
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return EdgeBiosEntityHandler.class;
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(InstallerEventModel.BIOS_DEPLOYMENT, new ModuleLoader(vertx));
    }

    protected void publishService(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        Observable.fromIterable(BiosInstallerService.createServices(this))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(
                                             service -> getEventController().register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(discovery, s))
                  .subscribe();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, BiosInstallerService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
