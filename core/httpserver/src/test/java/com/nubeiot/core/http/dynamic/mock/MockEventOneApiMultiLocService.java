package com.nubeiot.core.http.dynamic.mock;

import io.reactivex.Single;

import com.nubeiot.core.event.EventbusClient;
import com.nubeiot.core.http.base.event.EventMethodDefinition;
import com.nubeiot.core.micro.MicroContext;
import com.nubeiot.core.micro.ServiceDiscoveryController;

public class MockEventOneApiMultiLocService extends MockEventOneApiOneLocService {

    @Override
    public void registerEventbus(EventbusClient controller) {
        controller.register(MockEventServiceListener.TEST_EVENT_4, MockEventServiceListener.TEST_EVENT_LISTENER_4);
    }

    @Override
    protected void publishService(MicroContext microContext) {
        final ServiceDiscoveryController controller = microContext.getLocalController();
        Single.concat(controller.addEventMessageRecord("ems-4", MockEventServiceListener.TEST_EVENT_4.getAddress(),
                                                       EventMethodDefinition.createDefault("/p", "/:pId")),
                      controller.addEventMessageRecord("ems-4", MockEventServiceListener.TEST_EVENT_4.getAddress(),
                                                       EventMethodDefinition.createDefault("/c/:cId/p", "/:pId")))
              .subscribe();
    }

}
