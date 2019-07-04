package com.nubeiot.edge.bios.startupmodules;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.MockModuleLoader;
import com.nubeiot.edge.core.EdgeEntityHandler;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.edge.core.TransactionEventListener;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public class MockBiosStartupModulesVerticle extends EdgeBiosVerticle {

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
    private final Class<? extends EdgeEntityHandler> entityHandlerClass;

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
                            new ModuleEventListener(this, MockBiosStartupModulesVerticle.MOCK_BIOS_INSTALLER));
        controller.register(InstallerEventModel.BIOS_DEPLOYMENT, new MockModuleLoader(null));
        controller.register(InstallerEventModel.BIOS_TRANSACTION,
                            new TransactionEventListener(this, InstallerEventModel.BIOS_TRANSACTION));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
