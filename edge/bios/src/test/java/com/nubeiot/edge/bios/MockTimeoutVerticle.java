package com.nubeiot.edge.bios;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.core.ModuleEventListener;
import com.nubeiot.eventbus.edge.EdgeInstallerEventBus;

public class MockTimeoutVerticle extends EdgeBiosVerticle {

    static EventModel MOCK_TIME_OUT_INSTALLER = EventModel.builder()
                                                          .address("mockup.nubeiot.edge.bios.timeout")
                                                          .pattern(EventPattern.REQUEST_RESPONSE)
                                                          .addEvents(EventAction.PATCH, EventAction.CREATE)
                                                          .build();

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER,
                             new ModuleEventListener(this, MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER));
        eventClient.register(EdgeInstallerEventBus.BIOS_DEPLOYMENT, new MockTimeoutLoader());
    }

    @Override
    public String configFile() {
        return "mock-verticle.json";
    }

}
