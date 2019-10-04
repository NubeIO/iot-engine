package com.nubeiot.edge.module.installer;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.vertx.servicediscovery.Record;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.MicroserviceProvider;
import com.nubeiot.core.micro.ServiceDiscoveryController;
import com.nubeiot.edge.core.InstallerEntityHandler;
import com.nubeiot.edge.core.InstallerVerticle;
import com.nubeiot.edge.core.loader.ModuleLoader;
import com.nubeiot.edge.core.loader.ModuleTypeRule;
import com.nubeiot.edge.module.installer.service.EdgeInstallerService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public final class EdgeServiceInstallerVerticle extends InstallerVerticle {

    @Override
    public void start() {
        super.start();
        addProvider(new MicroserviceProvider(), this::publishService);
    }

    @Override
    protected Supplier<ModuleTypeRule> getModuleRuleProvider() {
        return new ServiceInstallerRuleProvider();
    }

    @Override
    protected Class<? extends InstallerEntityHandler> entityHandlerClass() {
        return ServiceInstallerEntityHandler.class;
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(InstallerEventModel.SERVICE_DEPLOYMENT, new ModuleLoader(vertx));
    }

    private void publishService(MicroContext microContext) {
        final ServiceDiscoveryController discovery = microContext.getLocalController();
        if (!discovery.isEnabled()) {
            return;
        }
        Observable.fromIterable(EdgeInstallerService.createServices(this))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(
                                             service -> getEventController().register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .flatMap(s -> registerEndpoint(discovery, s))
                  .subscribe();
    }

    private Observable<Record> registerEndpoint(ServiceDiscoveryController discovery, EdgeInstallerService s) {
        return Observable.fromIterable(s.definitions())
                         .flatMapSingle(e -> discovery.addEventMessageRecord(s.api(), s.address(), e));
    }

}
