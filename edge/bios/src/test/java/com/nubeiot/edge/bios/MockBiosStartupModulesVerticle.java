package com.nubeiot.edge.bios;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

    private final Class<? extends EdgeEntityHandler> entityHandlerClass;

    public static EventModel MOCK_BIOS_INSTALLER = EventModel.builder()
                                                             .address("mockup.nubeiot.edge.bios.installer")
                                                             .pattern(EventPattern.REQUEST_RESPONSE)
                                                             .events(Arrays.asList(EventAction.INIT, EventAction.CREATE,
                                                                                   EventAction.GET_ONE,
                                                                                   EventAction.GET_LIST,
                                                                                   EventAction.PATCH,
                                                                                   EventAction.REMOVE,
                                                                                   EventAction.UPDATE))
                                                             .build();

    public MockBiosStartupModulesVerticle(Class<? extends EdgeEntityHandler> entityHandlerClass) {
        this.entityHandlerClass = entityHandlerClass;
    }

    @Override
    protected Class<? extends EdgeEntityHandler> entityHandlerClass() {
        return this.entityHandlerClass;
    }

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(MockBiosStartupModulesVerticle.MOCK_BIOS_INSTALLER,
                            new ModuleEventHandler(this, MockBiosStartupModulesVerticle.MOCK_BIOS_INSTALLER));
        controller.register(EdgeInstallerEventBus.BIOS_DEPLOYMENT, new MockModuleLoader(null));
        controller.register(EdgeInstallerEventBus.BIOS_TRANSACTION,
                            new TransactionEventHandler(this, EdgeInstallerEventBus.BIOS_TRANSACTION));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
