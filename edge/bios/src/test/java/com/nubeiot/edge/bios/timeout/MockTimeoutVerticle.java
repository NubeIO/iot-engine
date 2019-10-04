package com.nubeiot.edge.bios.timeout;

import com.nubeiot.core.event.EventAction;
import com.nubeiot.core.event.EventController;
import com.nubeiot.core.event.EventModel;
import com.nubeiot.core.event.EventPattern;
import com.nubeiot.edge.bios.EdgeBiosVerticle;
import com.nubeiot.edge.bios.service.BiosModuleService;
import com.nubeiot.eventbus.edge.installer.InstallerEventModel;

public class MockTimeoutVerticle extends EdgeBiosVerticle {

    static EventModel MOCK_TIME_OUT_INSTALLER = EventModel.builder()
                                                          .address("mockup.nubeiot.edge.bios.timeout")
                                                          .pattern(EventPattern.REQUEST_RESPONSE)
                                                          .addEvents(EventAction.PATCH, EventAction.CREATE)
                                                          .build();

    @Override
    public void registerEventbus(EventController eventClient) {
        eventClient.register(MockTimeoutVerticle.MOCK_TIME_OUT_INSTALLER, new BiosModuleService(this));
        eventClient.register(InstallerEventModel.BIOS_DEPLOYMENT, new MockTimeoutLoader());
    }

}
