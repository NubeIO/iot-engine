package com.nubeiot.edge.bios;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockBiosEdgeVerticle extends EdgeBiosVerticle {

    static EventModel MOCK_BIOS_INSTALLER = EventModel.builder()
                                                      .address("mockup.nubeiot.edge.bios.installer")
                                                      .pattern(EventPattern.REQUEST_RESPONSE)
                                                      .events(Arrays.asList(EventAction.INIT, EventAction.CREATE,
                                                                            EventAction.GET_ONE, EventAction.GET_LIST,
                                                                            EventAction.PATCH, EventAction.REMOVE,
                                                                            EventAction.UPDATE))
                                                      .build();
    private final DeploymentAsserter deploymentAsserter;
    private final boolean failed;

    MockBiosEdgeVerticle(DeploymentAsserter deploymentAsserter) {
        this(deploymentAsserter, false);
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER,
                            new ModuleEventHandler(this, MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER));
        controller.register(EdgeInstallerEventBus.BIOS_DEPLOYMENT, failed ? new MockFailedModuleLoader(
            deploymentAsserter) : new MockModuleLoader(deploymentAsserter));
        controller.register(EdgeInstallerEventBus.BIOS_TRANSACTION,
                            new TransactionEventHandler(this, EdgeInstallerEventBus.BIOS_TRANSACTION));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
