package com.nubeiot.edge.bios;

import java.util.Arrays;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.core.ModuleEventHandler;
import com.nubeiot.edge.core.TransactionEventHandler;
import com.nubeiot.eventbus.edge.EdgeEventBus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockBiosEdgeVerticle extends EdgeBiosVerticle {

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
    private final AssertmentConsumer assertmentConsumer;

    @Override
    public void registerEventbus(EventController controller) {
        controller.register(MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER,
                            new ModuleEventHandler(this, MockBiosEdgeVerticle.MOCK_BIOS_INSTALLER));
        controller.register(EdgeEventBus.BIOS_DEPLOYMENT, new MockModuleLoader(assertmentConsumer));
        controller.register(EdgeEventBus.BIOS_TRANSACTION,
                            new TransactionEventHandler(this, EdgeEventBus.BIOS_TRANSACTION));
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
