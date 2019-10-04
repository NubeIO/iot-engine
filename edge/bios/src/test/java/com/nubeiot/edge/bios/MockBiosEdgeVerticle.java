package com.nubeiot.edge.bios;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.edge.bios.loader.DeploymentAsserter;
import com.nubeiot.edge.bios.loader.MockFailedModuleLoader;
import com.nubeiot.edge.bios.loader.MockModuleLoader;
import com.nubeiot.edge.bios.service.BiosInstallerService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MockBiosEdgeVerticle extends EdgeBiosVerticle {

    private final DeploymentAsserter deploymentAsserter;
    private final boolean failed;

    MockBiosEdgeVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, false);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        final @NonNull
        EventListener moduleLoader = failed
                                     ? new MockFailedModuleLoader(deploymentAsserter)
                                     : new MockModuleLoader(deploymentAsserter);
        eventClient.register(InstallerEventModel.BIOS_DEPLOYMENT, moduleLoader);
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
    public String configFile() {
        return "mock-verticle.json";
    }

}
