package com.nubeiot.edge.bios.startupmodules;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.MockModuleLoader;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private static EventModel MOCK_BIOS_INSTALLER = EventModel.builder()
                                                              .address("mockup.nubeiot.edge.bios.installer")
                                                              .pattern(EventPattern.REQUEST_RESPONSE)
                                                              .addEvents(EventAction.INIT, EventAction.CREATE,
                                                                         EventAction.GET_ONE, EventAction.GET_LIST,
                                                                         EventAction.PATCH, EventAction.REMOVE,
                                                                         EventAction.UPDATE)
                                                              .build();
    private final Class<? extends EdgeEntityHandler> entityHandlerClass;

    MockBiosStartupModulesVerticle(Class<? extends EdgeEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(MockBiosStartupModulesVerticle.MOCK_BIOS_INSTALLER,
                             new ModuleEventListener(this, MockBiosStartupModulesVerticle.MOCK_BIOS_INSTALLER));
        eventClient.register(EdgeInstallerEventBus.BIOS_DEPLOYMENT, new MockModuleLoader(null));
        eventClient.register(EdgeInstallerEventBus.BIOS_TRANSACTION,
                             new TransactionEventListener(this, EdgeInstallerEventBus.BIOS_TRANSACTION));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
