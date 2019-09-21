package com.nubeiot.edge.bios;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventListener;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MockBiosEdgeVerticle extends EdgeBiosVerticle {

    static EventModel MOCK_BIOS_INSTALLER = EventModel.builder()
                                                      .address("mockup.nubeiot.edge.bios.installer")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                 EventAction.GET_ONE, EventAction.GET_LIST,
                                                                 EventAction.PATCH, EventAction.REMOVE,
                                                                 EventAction.UPDATE)
                                                      .build();
    private final DeploymentAsserter deploymentAsserter;
    private final boolean failed;

    MockBiosEdgeVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, false);
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        final @NonNull EventListener moduleLoader = failed
                                                    ? new MockFailedModuleLoader(deploymentAsserter)
                                                    : new MockModuleLoader(deploymentAsserter);
        eventClient.register(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER,
                             new ModuleEventListener(this, MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER))
                   .register(InstallerEventModel.BIOS_TRANSACTION,
                             new TransactionEventListener(this, InstallerEventModel.BIOS_TRANSACTION))
                   .register(InstallerEventModel.BIOS_DEPLOYMENT, moduleLoader);
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
