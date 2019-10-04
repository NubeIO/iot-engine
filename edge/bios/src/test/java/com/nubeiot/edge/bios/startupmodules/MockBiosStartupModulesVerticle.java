package com.nubeiot.edge.bios.startupmodules;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.loader.MockModuleLoader;
import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private final Class<? extends EdgeEntityHandler> entityHandlerClass;

    MockBiosStartupModulesVerticle(Class<? extends EdgeEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(InstallerEventModel.BIOS_DEPLOYMENT, new MockModuleLoader(null));
    }

    @Override
    protected void publishService(MicroContext microContext) {
        Observable.fromIterable(BiosInstallerService.createServices(this))
                  .doOnEach(s -> Optional.ofNullable(s.getValue())
                                         .ifPresent(
                                             service -> getEventController().register(service.address(), service)))
                  .filter(s -> Objects.nonNull(s.definitions()))
                  .subscribe();
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
